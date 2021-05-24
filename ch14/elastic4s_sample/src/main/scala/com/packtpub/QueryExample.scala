package com.packtpub

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.Indexable
import com.sksamuel.elastic4s.circe._
import io.circe.generic.auto._

object QueryExample extends App with ElasticSearchClientTrait {
  val indexName = "myindex"

  case class Place(id: Int, name: String)

  case class Cafe(name: String, place: Place)

  implicitly[Indexable[Cafe]]

  ensureIndexMapping(indexName)

  client.execute {
    bulk(
      indexInto(indexName)
        .id("0")
        .source(Cafe("nespresso", Place(20, "Milan"))),
      indexInto(indexName)
        .id("1")
        .source(Cafe("java", Place(60, "Rome"))),
      indexInto(indexName)
        .id("2")
        .source(Cafe("nespresso", Place(70, "Paris"))),
      indexInto(indexName)
        .id("3")
        .source(Cafe("java", Place(80, "Chicago"))),
      indexInto(indexName)
        .id("4")
        .source(Cafe("nespresso", Place(10, "London"))),
      indexInto(indexName)
        .id("5")
        .source(Cafe("java", Place(60, "Milan"))),
      indexInto(indexName)
        .id("6")
        .source(Cafe("nespresso", Place(25, "Rome"))),
      indexInto(indexName)
        .id("7")
        .source(Cafe("java", Place(56, "Paris"))),
      indexInto(indexName)
        .id("8")
        .source(Cafe("nespresso", Place(23, "Chicago"))),
      indexInto(indexName)
        .id("9")
        .source(Cafe("java", Place(89, "London")))
    )
  }.await

  Thread.sleep(2000)

  val resp = client.execute {
    search(indexName).bool(
      must(termQuery("name", "java"), rangeQuery("place.id").gte(80)))
  }.await

  println(resp.result.size)

  println(resp.result.to[Cafe].toList)

  //client.execute(deleteIndex(indexName)).await

  client.close()
}
