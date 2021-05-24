package com.packtpub

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.circe._

object DocumentExample extends App with ElasticSearchClientTrait {
  val indexName = "myindex"

  ensureIndexMapping(indexName)

  client.execute {
    indexInto(indexName) id "0" fields (
      "name" -> "brown",
      "tag" -> List("nice", "simple")
    )
  }.await

  val bwn = client.execute {
    get(indexName, "0")
  }.await

  println(bwn.result.sourceAsString)

  client.execute {
    updateById(indexName, "0").script("ctx._source.name = 'red'")
  }.await

  val red = client.execute {
    get(indexName, "0")
  }.await

  println(red.result.sourceAsString)

  client.execute {
    deleteById(indexName, "0")
  }.await

  case class Place(id: Int, name: String)
  case class Cafe(name: String, place: Place)

  import io.circe.generic.auto._
  import com.sksamuel.elastic4s.Indexable
  implicitly[Indexable[Cafe]]

  val cafe = Cafe("nespresso", Place(20, "Milan"))

  client.execute {
    indexInto(indexName).id(cafe.name).source(cafe)
  }.await

  client.execute(deleteIndex(indexName)).await

  client.close()
}
