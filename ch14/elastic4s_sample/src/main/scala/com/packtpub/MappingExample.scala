package com.packtpub

import com.sksamuel.elastic4s.ElasticDsl._

object MappingExample extends App with ElasticSearchClientTrait {
  val indexName = "myindex"
  if (client.execute { indexExists(indexName) }.await.result.isExists) {
    client.execute { deleteIndex(indexName) }.await
  }

  client.execute {
    createIndex(indexName) shards 1 replicas 0 mapping (
      properties(
        textField("name").termVector("with_positions_offsets").stored(true)
      )
    )
  }.await
  Thread.sleep(2000)

  client.execute {
    putMapping(indexName).as(
      keywordField("tag")
    )
  }.await

  val myMapping = client
    .execute {
      getMapping(indexName)
    }
    .await
    .result

  val tagMapping = myMapping.head
  println(tagMapping)


  client.execute(deleteIndex(indexName)).await

  //we need to close the client to free resources
  client.close()
}
