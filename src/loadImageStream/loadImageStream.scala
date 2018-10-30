import org.apache.spark.ml.image.ImageSchema._
import java.nio.file.Paths
import org.apache.spark.sql._

import java.util.HashMap
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.streaming._
import org.apache.spark.storage.StorageLevel
import java.util.{Date, Properties}



object LoadImageStream {
   def main(args: Array[String]): Unit = {


     val sparkConf = new SparkConf().setAppName("KafkaSparkFaceDetection").setMaster("local[2]")

     // Create a StreamingContext, batchduration could be 5s subject to change.
     val sc = SparkContext.getOrCreate(sparkConf)

     // Reduce the log messages. We don't want Spark filling the console, except in case of error.
     sc.setLogLevel("ERROR")

     // should read from HDFS
     val images = readImages("/Users/philipclaesson/ML/DIC/project/data/INRIAPerson/Test/pos/")


     images.foreach { rrow =>
      val row = rrow.getAs[Row](0)
      val filename = Paths.get(getOrigin(row)).getFileName().toString()
      val imageData = getData(row)
      val height = getHeight(row)
      val width = getWidth(row)

      println(s"${height}x${width}")
    }
  }
}
