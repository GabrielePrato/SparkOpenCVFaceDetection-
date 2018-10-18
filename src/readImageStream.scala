package sparkstreaming

import java.util.HashMap
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka._
import kafka.serializer.{DefaultDecoder, StringDecoder}
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.storage.StorageLevel
import java.util.{Date, Properties}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, ProducerConfig}
import org.apache.spark.sql.cassandra._
import com.datastax.spark.connector._
import com.datastax.driver.core.{Session, Cluster, Host, Metadata}
import com.datastax.spark.connector.streaming._


object KafkaSpark {
  def main(args: Array[String]) {

    // Kafka connection settings
    val kafkaConf = Map(
      "metadata.broker.list" -> "localhost:9092",
      "zookeeper.connect" -> "localhost:2181",
      "group.id" -> "kafka-spark-streaming",
      "zookeeper.connection.timeout.ms" -> "1000"
    )

    // Set topic to read from
    val topic = Set("images")

    val sparkConf = new SparkConf().setAppName("KafkaMovingAverage").setMaster("local[2]")

    // Create a StreamingContext, batchduration could be 5s subject to change.
    val ssc = new StreamingContext(sparkConf, Seconds(5))

    // checkpointing for StreamingContexts
    ssc.checkpoint("../data/streamCheckpoints")

    // Reduce the log messages. We don't want Spark filling the console, except in case of error.
    ssc.sparkContext.setLogLevel("ERROR")

    // Create stream from Kafka to Spark. We chose to start a direct, Receiver-less stream as it has numerous advantages over receiver-based.

    //// ?? We need to provide an image decoder, which complies wth how images are decoded in the producer.
    //// Should get the producer running and also look into how it works first
    val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaConf, topic)


    // print something here to show that we are receiving images.. System.out.print("something")


    ssc.start()
    ssc.awaitTermination()
    ssc.stop()
  }
}
