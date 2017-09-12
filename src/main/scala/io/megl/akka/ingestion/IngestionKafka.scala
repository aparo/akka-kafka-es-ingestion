package io.megl.akka.ingestion

import java.io.InputStreamReader

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, ClosedShape}
import com.github.tototoshi.csv.CSVReader
import io.megl.akka.ingestion.domain.{EnrichedValue, Value}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

object IngestionKafka extends IngestionTrait {

  def goodMessage(elem: EnrichedValue) = new ProducerRecord[Array[Byte], String]("enriched", elem.toString)

  def deadLetterMessage(elem: EnrichedValue) = new ProducerRecord[Array[Byte], String]("deadletters", elem.toString)


  def main(args: Array[String]): Unit = {
    val ip = if(args.length>0) args(0) else "127.0.0.1"

    implicit var system = ActorSystem("IngestionSystem")
    implicit val materializer = ActorMaterializer()

    val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers(s"${ip}:9092")

    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val src = createCsvSource()
      val transformFlow = Flow[Seq[String]].map(in => Value(in.head.toInt, in(1)))
      val enrichFlow = Flow[Value].map(in => EnrichedValue(in, 2))
      val bcast = builder.add(Broadcast[Value](2))
      val filterValid = Flow[Value].filter(i => i.val1 >= 2)
      val filterInvalid = Flow[Value].filter(i => i.val1 < 2)

      val sink = Sink.ignore

      src ~> transformFlow ~> bcast ~> filterValid ~> enrichFlow.map(goodMessage) ~> Producer.plainSink(producerSettings)
                              bcast ~> filterInvalid ~> enrichFlow.map(deadLetterMessage) ~> Producer.plainSink(producerSettings)
      ClosedShape
    }).run()
  }
}
