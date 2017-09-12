package io.megl.akka.ingestion

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink}
import akka.stream.{ActorMaterializer, ClosedShape}
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.bulk.BulkCompatibleDefinition
import com.sksamuel.elastic4s.http.HttpClient
import io.megl.akka.ingestion.domain.{EnrichedValue, Value}
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import com.sksamuel.elastic4s.streams.ReactiveElastic._
import com.sksamuel.elastic4s.circe._
import io.circe.generic.auto._
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.streams.RequestBuilder

object IngestionElasticSearch extends IngestionTrait {

  def main(args: Array[String]): Unit = {
    val ip = if(args.length>0) args(0) else "127.0.0.1"

    implicit var system = ActorSystem("IngestionSystem")
    implicit val materializer = ActorMaterializer()

    val client = HttpClient(ElasticsearchClientUri(ip, 9200))

    client.execute { createIndex("valid-data") shards 3 replicas 0 }
    client.execute { createIndex("invalid-data") shards 3 replicas 0 }

    implicit val validBuilder = new RequestBuilder[EnrichedValue] {
      // the request returned doesn't have to be an index - it can be anything supported by the bulk api
      def request(t: EnrichedValue): BulkCompatibleDefinition =  {
        indexInto("valid-data",  "type").source(t)
      }
    }

    implicit val invalidBuilder = new RequestBuilder[EnrichedValue] {
      // the request returned doesn't have to be an index - it can be anything supported by the bulk api
      def request(t: EnrichedValue): BulkCompatibleDefinition =  {
        indexInto("invalid-data",  "type").source(t)
      }
    }

    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val src = createCsvSource()
      val transformFlow = Flow[Seq[String]].map(in => Value(in.head.toInt, in(1)))
      val enrichFlow = Flow[Value].map(in => EnrichedValue(in, 2))
      val bcast = builder.add(Broadcast[Value](2))
      val filterValid = Flow[Value].filter(i => i.val1 >= 2)
      val filterInvalid = Flow[Value].filter(i => i.val1 < 2)

      val goodSubscriber = client.subscriber[EnrichedValue](batchSize=5, concurrentRequests=2,completionFn= () => println("valid: all done"))(validBuilder, system)
      val badSubscriber = client.subscriber[EnrichedValue](batchSize=5, concurrentRequests=2,completionFn= () => println("invalid: all done"))(invalidBuilder, system)

      val sink = Sink.ignore

      src ~> transformFlow ~> bcast ~> filterValid ~> enrichFlow ~> Sink.fromSubscriber(goodSubscriber)
                              bcast ~> filterInvalid ~> enrichFlow ~> Sink.fromSubscriber(badSubscriber)
      ClosedShape
    }).run()
  }
}
