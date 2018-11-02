import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp._

object ImageContainer {

  /**
   * Builds a
   */
  def build(color: Mat): ImageContainer = {
    val grey = toGreyScale(color)
    ImageContainer(color, grey)
  }

  def toGreyScale(mat: Mat): Mat = {
    val greyMat = {
      val (rows, cols) = (mat.rows(), mat.cols())
      new Mat(rows, cols, CV_8U)
    }
    opencv_imgproc.cvtColor(mat, greyMat, opencv_imgproc.COLOR_BGR2GRAY, 1)
    greyMat
  }

}

final case class ImageContainer private (orig: Mat, grey: Mat)
