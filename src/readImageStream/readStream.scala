
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
import org.bytedeco.javacpp._
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacv.{OpenCVFrameConverter, Java2DFrameConverter}
import java.nio.ByteBuffer
import java.io.File
import javax.imageio.ImageIO
import java.awt.image.{BufferedImage, DataBufferByte}
import java.util.{Date, Properties}



object readStream {
  def main(args: Array[String]) {

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
        //val path = row.getAs("path") // Getting errors here now:
        val path  = row.getString(0)
        val height = ImageSchema.getHeight(row)
        val width = ImageSchema.getWidth(row)
        val ocvType = ImageSchema.getMode(row)
        val bytes = Base64.getDecoder().decode(ImageSchema.getData(row))

        // Create byte buffer
        val bb = ByteBuffer.wrap(bytes)
        // Create a pointer to the bytebuffer to include in the mat
        val p = new Pointer(bb)
        // Construct mat
        val img = new Mat(height, width, ocvType, p)

        System.out.print("Converted to mat")
        (path, img)
      }

      def toGreyScale(orig: (String, Mat)): (String, Mat) = {
        val path = orig._1
        val orig_mat = orig._2

        // Get dimensions of img and create gray matrix of same dimension
        val dimensions = new Size(orig_mat.rows(), orig_mat.cols())
        val grey_mat = new Mat(dimensions, CV_8U)

        // Create grayscale img
        opencv_imgproc.cvtColor(orig_mat, grey_mat, opencv_imgproc.COLOR_BGR2GRAY, 1) // COLOR_BGR2GRAY = 6
        (path, grey_mat)
      }

      def histogramEqualizer(grey_img: (String, Mat)): (String, Mat) = {
        val path = grey_img._1
        val grey = grey_img._2

        //create new equalized Mat
        val equal = new Mat()
        opencv_imgproc.equalizeHist(grey, equal)
        (path, equal)
      }

      def faceDetector(equalized: (String, Mat)): (String, Rect) = {
        val path = equalized._1
        val equal = equalized._2

        //operate the detection
        val classLoader = this.getClass.getClassLoader
        val faceXml = classLoader.getResource("haarcascade_frontalface_alt.xml").getPath
        val faceCascade = new CascadeClassifier(faceXml)
        val faceRects = new Rect()
        faceCascade.detectMultiScale(equal, faceRects)
        (path, faceRects)
      }

      def BoundaryDrawer(rect: (String, Rect), orig: (Mat)): (String, Any) = {
        val path = rect._1
        val rectangles = rect._2

        //draw squares surrounding detected faces
        val image = orig
        val RedColour = new Scalar(AbstractCvScalar.RED)
        //val graphics = image.getGraphics
        //graphics.setColor(Color.RED)
        //graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18))

        rectangle(
          image,
          new Point(rectangles.x, rectangles.y),
          new Point(rectangles.x + rectangles.width, rectangles.y + rectangles.height),
          RedColour,
          1,
          CV_AA,
          0
        )

/*
        for(i <- 0L until rectangles.limit()) {
          val faceRect = rectangles.position(i)
          graphics.drawRect(faceRect.x, faceRect.y, faceRect.width, faceRect.height)
        }
        */
        //ImageIO.write(image, &quot;jpg&quot;, new File(&quot;output_faces.jpg&quot;))
        //TODO integrate with writing in hdfs
        ("test", 0)
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