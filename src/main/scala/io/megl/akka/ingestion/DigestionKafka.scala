package io.megl.akka.ingestion

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

object DigestionKafka {

  def main(args: Array[String]): Unit = {
    val ip = if (args.length > 0) args(0) else "127.0.0.1:9092"

    implicit var system = ActorSystem("DigestionSystem")
    implicit val materializer = ActorMaterializer()
    val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers(s"${ip}")
      .withGroupId("group1")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    //,Set("valid")

    Consumer.committableSource(consumerSettings.withClientId("client9"),
      Subscriptions.topics("enriched"))
      .runForeach(i => {
        println(s"${i.record.key()} -> ${i.record.value()}")
        i.committableOffset.commitScaladsl()
      })

  }
}
