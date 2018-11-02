name := "Face Detection Project"
version := "1.0"
scalaVersion := "2.11.12"
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.3.1",
  "org.apache.spark" % "spark-streaming_2.11" % "2.3.1",
  "org.apache.spark" %% "spark-mllib" % "2.3.1",
  "org.bytedeco" % "javacv" % "1.4.3",
  "org.bytedeco" % "javacpp" % "1.4.3"
)

