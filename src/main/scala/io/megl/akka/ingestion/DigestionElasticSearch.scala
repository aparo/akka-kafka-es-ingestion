package io.megl.akka.ingestion

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.HttpClient
import com.sksamuel.elastic4s.streams.ReactiveElastic._
import io.circe._
import io.megl.akka.ingestion.domain.EnrichedValue

object DigestionElasticSearch {

  def main(args: Array[String]): Unit = {
    val ip = if (args.length > 0) args(0) else "127.0.0.1"

    implicit var system = ActorSystem("DigestionSystem")
    implicit val materializer = ActorMaterializer()

    val client = HttpClient(ElasticsearchClientUri(ip, 9200))


    Source.fromPublisher(client.publisher(search("valid-data") query matchAllQuery() scroll "1m"))
      .mapConcat(r => parser.parse(r.sourceAsString).flatMap(_.as[EnrichedValue]).toOption.toList)
      .filter(enrichedValue => enrichedValue.val3 == 2)
      .runForeach(i => println(i))


  }
}
