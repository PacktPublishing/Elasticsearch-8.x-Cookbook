package com.packtpub

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.CsvParsing
import akka.stream.alpakka.elasticsearch.scaladsl.ElasticsearchFlow
import akka.stream.alpakka.elasticsearch.{ElasticsearchWriteSettings, RetryAtFixedRate, WriteMessage}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import better.files.Resource
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient

import scala.concurrent.Await
import scala.concurrent.duration._

final case class Iris(label: String, f1: Double, f2: Double, f3: Double, f4: Double)

object CSVToES extends App {

  implicit val actorSystem = ActorSystem()
  implicit val actorMaterializer = ActorMaterializer()
  implicit val executor = actorSystem.dispatcher
  implicit val client: RestClient = RestClient.builder(new HttpHost("0.0.0.0", 9200)).build()

  final case class Iris(label: String, f1: Double, f2: Double, f3: Double, f4: Double)

  import spray.json._
  import DefaultJsonProtocol._

  implicit val format: JsonFormat[Iris] = jsonFormat5(Iris)

  val sinkSettings =
    ElasticsearchWriteSettings()
      .withBufferSize(1000)
      .withVersionType("internal")
      .withRetryLogic(RetryAtFixedRate(maxRetries = 5, retryInterval = 1.second))

  val graph = Source.single(ByteString(Resource.getAsString("com/packtpub/iris.csv")))
    .via(CsvParsing.lineScanner())
    .drop(1)
    .map(values => WriteMessage.createIndexMessage[Iris](
      Iris(
        values(4).utf8String,
        values.head.utf8String.toDouble,
        values(1).utf8String.toDouble, values(2).utf8String.toDouble,
        values(3).utf8String.toDouble)))
    .via(
      ElasticsearchFlow.create[Iris](
        "iris-alpakka",
        "_doc",
        settings = sinkSettings
      ))
    .runWith(Sink.ignore)

  val finish = Await.result(graph, Duration.Inf)
  client.close()
  actorSystem.terminate()
  Await.result(actorSystem.whenTerminated, Duration.Inf)

}
