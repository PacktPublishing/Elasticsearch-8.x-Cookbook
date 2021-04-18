package com.packtpub

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.requests.searches.aggs.responses.bucket.Terms

object AggregationExample extends App with ElasticSearchClientTrait {
  val indexName = "myindex"
  ensureIndexMapping(indexName)
  populateSampleData(indexName, 1000)

  val resp = client
    .execute {
      search(indexName) size 0 aggregations (termsAgg(
        "tag",
        "tag"
      ) size 100 subAggregations (
        extendedStatsAgg("price", "price"),
        extendedStatsAgg("size", "size"),
        geoBoundsAggregation("centroid") field "location"
      ))
    }
    .await
    .result

  val tagsAgg = resp.aggregations.result[Terms]("tag")

  println(s"Result Hits: ${resp.size}")
  println(s"number of tags: ${tagsAgg.buckets.size}")
  println(
    s"max price of first tag ${tagsAgg.buckets.head.key}: ${tagsAgg.buckets.head.extendedStats("price").max}"
  )
  println(
    s"min size of first tag ${tagsAgg.buckets.head.key}: ${tagsAgg.buckets.head.extendedStats("size").min}"
  )

  client.execute(deleteIndex(indexName)).await

  client.close()
}
