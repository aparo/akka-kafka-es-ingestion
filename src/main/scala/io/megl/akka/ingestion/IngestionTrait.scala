package io.megl.akka.ingestion

import java.io.InputStreamReader

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.github.tototoshi.csv.CSVReader
import io.megl.akka.ingestion.IngestionKafka.getClass
import io.megl.akka.ingestion.domain.EnrichedValue
import org.apache.kafka.clients.producer.ProducerRecord

trait IngestionTrait {


  def createCsvSource(): Source[Seq[String], NotUsed] = {
    Source.fromIterator(() => {
      val it = CSVReader
        .open(new InputStreamReader(
          getClass.getClassLoader.getResourceAsStream("values.csv")
        ))
        .iterator
      if (it.hasNext) it.next()
      it
    })
  }

}
