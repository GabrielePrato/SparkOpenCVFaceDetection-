package sparkstreaming

import java.util.HashMap
import java.util.Base64
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.streaming._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.ml.image.ImageSchema
import org.apache.spark.ml.image.ImageSchema._
import org.apache.spark.sql._
//import org.opencv.imgproc._
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp._
import org.bytedeco.javacpp.opencv_core.Mat
// import ImageContainer._
import FaceDetector._
// import java.nio.file.Paths
import java.util.{Date, Properties}


object readStream {
  def main(args: Array[String]) {

    // Set topic to read from
    val topic = Set("images")

    val sparkConf = new SparkConf().setAppName("SparkFaceDetection").setMaster("local[2]")

    // Create a StreamingContext, batchduration could be 5s subject to change.
    val sc = SparkContext.getOrCreate(sparkConf)

    // Reduce the log messages. We don't want Spark filling the console, except in case of error.
    sc.setLogLevel("ERROR")

    val raw_images = readImages("./data/INRIAPerson/Test/two_images/")

    // Print the schema of images
    raw_images.printSchema()

    /**
    * We need to convert imported images to Mat format.
    * Below rows do not work.
    */

    val images = raw_images.map(row2mat)

    // Using general Mat object for functionality instead.
    // val images = new Set(new Mat(640, 480))

    // Convert to grayscale
    val grayImages = images.map(toGreyScale);

    // Create FaceDetector object
    val faceDetector = new FaceDetector


    // from here not done yet, need to be implemented. But we should start with getting the images to work.
    /*
    val flow = images
      .map(faceDetector.detect)
      .map((faceDrawer.drawFaces _).tupled)
      .to(Sink.ignore)

    flow.run()
    */

    // detect and draw faces
    // do stuff here

    System.out.println("Stopping")

  }
      def toGreyScale(mat: Mat): Mat = {
        val greyMat = {
          val (rows, cols) = (mat.rows(), mat.cols())
          new Mat(rows, cols, CV_8U)
        }
        opencv_imgproc.cvtColor(mat, greyMat, opencv_imgproc.COLOR_BGR2GRAY, 1) // COLOR_BGR2GRAY = 6
        greyMat
      }

      def row2mat(row: Row): (String, Mat) = {
        val path    = row.getAs("path")
        val height  = row.getAs("height")
        val width   = row.getAs("width")
        val ocvType = row.getAs("mode")

        /** This creates an error:
        ambiguous reference to overloaded definition,
        [error] both method decode in class Decoder of type (x$1: java.nio.ByteBuffer)java.nio.ByteBuffer
        [error] and  method decode in class Decoder of type (x$1: String)Array[Byte]
        */
        // implicit val .......
        val bytes = row.getAs("data") //Base64.getDecoder().decode(row.getAs("data"))

        val img = new Mat(height, width, ocvType)
        img.put(0,0,bytes)
        (path, img)
      }

}

// holder for a single detected face: contains face rectangle and the two eye rectangles inside
case class Face(id: Int, faceRect: Rect, leftEyeRect: Rect, rightEyeRect: Rect)