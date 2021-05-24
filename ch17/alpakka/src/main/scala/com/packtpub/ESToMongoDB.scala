package com.packtpub

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.elasticsearch.scaladsl.ElasticsearchSource
import akka.stream.alpakka.mongodb.scaladsl.MongoSink
import com.mongodb.reactivestreams.client.MongoClients
import org.apache.http.HttpHost
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.elasticsearch.client.RestClient
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

import scala.concurrent.Await
import scala.concurrent.duration._

object ESToMongoDB extends App {

  implicit val actorSystem = ActorSystem()
  implicit val actorMaterializer = ActorMaterializer()
  implicit val executor = actorSystem.dispatcher

  implicit val elasticsearch: RestClient = RestClient.builder(new HttpHost("0.0.0.0", 9200)).build()


  import spray.json._
  import DefaultJsonProtocol._


  implicit val format: JsonFormat[Iris] = jsonFormat5(Iris)
  val codecRegistry = fromRegistries(fromProviders(classOf[Iris]), DEFAULT_CODEC_REGISTRY)

  private val mongo = MongoClients.create("mongodb://localhost:27017")
  private val db = mongo.getDatabase("es-to-mongo")
  val irisCollection = db
    .getCollection("iris", classOf[Iris])
    .withCodecRegistry(codecRegistry)

  val graph =
    ElasticsearchSource
      .typed[Iris](
      indexName = "iris-alpakka",
      typeName = "_doc",
      query = """{"match_all": {}}"""
    )
      .map(_.source) // we want only the source
      .grouped(100) // bulk insert of 100
      .runWith(MongoSink.insertMany[Iris](irisCollection))

  val finish = Await.result(graph, Duration.Inf)

  elasticsearch.close()
  mongo.close()
  actorSystem.terminate()
  Await.result(actorSystem.whenTerminated, Duration.Inf)

}
