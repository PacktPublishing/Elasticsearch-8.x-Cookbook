package com.packtpub

import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties}
import com.sksamuel.elastic4s.http._
import com.sksamuel.elastic4s.requests.common.HealthStatus

trait ElasticSearchClientTrait {
  lazy val client = ElasticClient(
    JavaClient(ElasticProperties(s"http://localhost:9200"))
  )

  def ensureIndexMapping(indexName: String): Unit = {
    if (client
          .execute {
            indexExists(indexName)
          }
          .await
          .result
          .isExists) {
      client.execute {
        deleteIndex(indexName)
      }.await
    }

    client.execute {
      createIndex(indexName) shards 1 replicas 0 mapping (
        properties(
          textField("name") termVector "with_positions_offsets" stored true,
          longField("size"),
          doubleField("price"),
          geopointField("location"),
          keywordField("tag") stored true
        )
      )
    }.await

    client.execute {
      clusterHealth() waitForStatus HealthStatus.Yellow
    }

  }

  def populateSampleData(indexName: String,
                         size: Int = 1000): Unit = {
    import scala.util.Random
    val tags = List("cool", "nice", "bad", "awesome", "good")
    client.execute {
      bulk(0.to(size).map { i =>
        indexInto(indexName)
          .id(i.toString)
          .fields(
            "name" -> s"name_${i}",
            "size" -> (i % 10) * 8,
            "price" -> (i % 10) * 1.2,
            "location" -> List(30.0 * Random.nextDouble(),
                               30.0 * Random.nextDouble()),
            "tag" -> Random.shuffle(tags).take(3)
          )
      }: _*)
    }.await

    Thread.sleep(2000)

  }
}
