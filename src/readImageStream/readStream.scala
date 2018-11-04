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

import org.bytedeco.javacv.{OpenCVFrameConverter, Java2DFrameConverter}

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
  def main(args: Array[String]) {
    // System.load("/opencv-343.jar");
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

    // Set topic to read from
    val topic = Set("images")

    // Create sparksession
    val ss = SparkSession.builder
      .master("local[2]")
      .appName("SparkFaceDetection")
      .getOrCreate()
    import ss.implicits._

    val raw_images = readImages("./../../data/INRIAPerson/Test/two_images/")

    // Print the schema of images
    raw_images.printSchema()

    // Convert to RDD because DataFrame causes errors: https://issues.apache.org/jira/browse/SPARK-17890
    val raw_images_rdd: RDD[Row] = raw_images.rdd

    // Print to see row structure
    raw_images_rdd.take(2).foreach(println)

    // apply row2mat, return tuple (path, mat)

    val orig_images = raw_images_rdd.map(x => rowToMat(x))

    // Convert to grayscale, return tuple (path, mat)
    val gray_images = orig_images.map(toGreyScale);

    // This line is just for testing and error detection
    gray_images.collect.foreach(println)

    // Save to file.
    gray_images.map(writeToFile)

    System.out.println("Stopping")

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

        System.out.print("Converted to mat")
        (path, img)
      }

      def toGreyScale(orig: (String, Mat)): (String, Mat) = {
        val path = orig._1
        val orig_mat = orig._2

        // Get dimensions of img and create gray matrix of same dimension
        val dimensions = new Size(orig_mat.rows(), orig_mat.cols())
        val grey_mat = new Mat(dimensions, CvType.CV_8U)

        // Create grayscale img
        Imgproc.cvtColor(orig_mat, grey_mat, Imgproc.COLOR_BGR2GRAY, 1) // COLOR_BGR2GRAY = 6
        (path, grey_mat)
      }

      def histogramEqualizer(grey_img: (String, Mat)): (String, Mat) = {
        val path = grey_img._1
        val grey = grey_img._2

        //create new equalized Mat
        val equal = new Mat()
        Imgproc.equalizeHist(grey, equal)
        (path, equal)
      }

      def faceDetector(equalized: (String, Mat)): (String, MatOfRect) = {
        val path = equalized._1
        val equal = equalized._2

        //operate the detection
        val classLoader = this.getClass.getClassLoader
        val faceXml = classLoader.getResource("haarcascade_frontalface_alt.xml").getPath
        val faceCascade = new CascadeClassifier(faceXml)
        val faceRects = new MatOfRect()
        faceCascade.detectMultiScale(equal, faceRects)
        (path, faceRects)
      }

      def BoundaryDrawer(rect: (String, MatOfRect), orig: (Mat)): (String, Mat) = {
        val path = rect._1
        val rectangles = rect._2

        //draw squares surrounding detected faces
        val image = orig

        for(r <- rectangles) {
          /*
          rectangle(
            image,
            new Point(r.x, r.y),
            new Point(r.x + r.width, r.y + r.height),
            AbstractCvScalar.RED,
            1,
            CV_AA,
            0
          )
          */
        }

        (path, image)
      }

      def writeToFile(img: (String, Mat)) = {
        val path = img._1
        val mat = img._2
        val m2fConverter = new OpenCVFrameConverter.ToOrgOpenCvCoreMat()
        val frame = m2fConverter.convert(mat)

        val f2iConverter = new Java2DFrameConverter()
        val image = f2iConverter.convert(frame)
        ImageIO.write(image, "png", new File("test_output.png"))
        System.out.print("Wrote to file")
      }
}