import java.util.HashMap
import java.util.Base64
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.streaming._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.ml.image.ImageSchema
import org.apache.spark.ml.image.ImageSchema._
import org.apache.spark.sql._
import org.apache.spark.rdd._


import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfRect
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.objdetect.CascadeClassifier

import java.nio.ByteBuffer
import java.io.File
import javax.imageio.ImageIO
import java.awt.image.{BufferedImage, DataBufferByte}
import java.util.{Date, Properties}
import java.awt.Graphics2D


object readStream {

  /**
    DEFINE PATHS FOR READING/WRITING LOCALLY OR TO HDFS.
  **/

  // Local directories sample data for testing

/*
  val INPUT_DIR = "./../data/SAMPLE_INPUT/"
  val OUTPUT_DIR = "./../data/SAMPLE_OUTPUT/"
*/

  // HDFS directories

  val INPUT_DIR = "hdfs://127.0.0.1:9000/images"
  val OUTPUT_DIR = "hdfs://127.0.0.1:9000/output"


  // Set hadoop username in order to gain write permission.
  // without it you can not write to HDFS.
  val HADOOP_USER_NAME = "philipclaesson"


  def main(args: Array[String]) {
    // Load OpenCV
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
    System.setProperty("HADOOP_USER_NAME", this.HADOOP_USER_NAME)

    // Create sparksession
    val ss = SparkSession.builder
      .master("local[2]")
      .appName("SparkFaceDetection")
      .getOrCreate()
    import ss.implicits._

    // Read images
    val raw_images = readImages(this.INPUT_DIR)

    // Print the schema of images
    raw_images.printSchema()

    // Convert to RDD
    val raw_images_rdd: RDD[Row] = raw_images.rdd

    // Access the data in byteform, store in Mat object
    val orig_images = raw_images_rdd.map(x => rowToMat(x))
    // System.out.println("orig_images size: " + orig_images.count().toString)

    // Convert to grayscale, return tuple (path, mat)
    val gray_images = orig_images.map(x => toGrayScale(x));
    // System.out.println("gray_images size: " + gray_images.count().toString)

    // Equalize
    val equalized_images = gray_images.map(x => histogramEqualizer(x))
    // System.out.println("equalized_images size: " + equalized_images.count().toString)

    // Apply face detection
    val detected_faces = equalized_images.map(x => faceDetector(x))
    // System.out.println("detected_faces size: " + detected_faces.count().toString)

    // Draw faces (rectangles) to images and save to HDFS
    val final_images = detected_faces.map(x => rectDrawer(x))

    System.out.println("Finished. Number of processed images: " + final_images.count().toString)

  }
      def rowToMat(row: Row): (String, Mat) = {
        // Get image
        val image = row.get(0).asInstanceOf[Row]

        // Get image attributes and data
        val path = ImageSchema.getOrigin(image)
        val height = ImageSchema.getHeight(image)
        val width = ImageSchema.getWidth(image)
        val ocvType = ImageSchema.getMode(image)
        val bytes = ImageSchema.getData(image)

        // Construct mat
        val img = new Mat(height, width, ocvType)
        img.put(0, 0, bytes)

        (path, img)
      }

      def toGrayScale(img: (String, Mat)): (String, Mat, Mat) = {
        val path = img._1
        val orig = img._2

        // Get dimensions of img and create gray matrix of same dimension
        val dimensions = new Size(orig.rows(), orig.cols())
        val grey = new Mat(dimensions, CvType.CV_8U)

        // Create grayscale img
        Imgproc.cvtColor(orig, grey, Imgproc.COLOR_BGR2GRAY, 1) // COLOR_BGR2GRAY = 6

        (path, orig, grey)
      }

      def histogramEqualizer(grey_img: (String, Mat, Mat)): (String, Mat, Mat) = {
        val path = grey_img._1
        val orig = grey_img._2
        val grey = grey_img._3

        //Create new equalized Mat
        val equal = new Mat()
        Imgproc.equalizeHist(grey, equal)

        (path, orig, equal)
      }

      def faceDetector(img: (String, Mat, Mat)): (String, Mat, MatOfRect) = {
        val path = img._1
        val orig = img._2
        val equal = img._3

        //operate the detection
        val faceCascade = new CascadeClassifier("haarcascade_frontalface_alt.xml")
        val faceRects = new MatOfRect()
        faceCascade.detectMultiScale(equal, faceRects)

        (path, orig, faceRects)
      }

      def rectDrawer(img: (String, Mat, MatOfRect)): (String, Mat) = {
        // Draw squares surrounding detected faces
        val path = img._1
        val orig = img._2
        val rectangles = img._3

        // for each rectangle (corresponding to a detected face): draw a rectangle
        for(r <- rectangles.toArray) {
          Imgproc.rectangle(
            orig,
            new Point(r.x, r.y),
            new Point(r.x + r.width, r.y + r.height),
            new Scalar(0, 255, 0)
          )
        }

        //
        // Write image to filesystem
        //

        // Extract filename from path
        val components = path.split("/")
        val filename = components(components.length - 1)

        // Write to local or hdfs
        Imgcodecs.imwrite(this.OUTPUT_DIR + filename, orig)

        (path, orig)
      }
}