# SparkOpenCVFaceDetection-
A simple face detection tool constructed in order to be scalable and distributed, implemented through Spark and OpenCV.


## How to run the code
- Start nodes
```
$HADOOP_HOME/sbin/hadoop-daemon.sh start namenode
$HADOOP_HOME/sbin/hadoop-daemon.sh start datanode
```

- Create directory /images and /output in HDFS
```
$HADOOP_HOME/bin/hdfs dfs -mkdir /images
$HADOOP_HOME/bin/hdfs dfs -mkdir /output
```


- Add images to /images in HDFS, for example:
```
$HADOOP_HOME/bin/hdfs dfs -put ./data/SAMPLE_INPUT/* /images
```


- Run face detection
`sbt run` in `/src`

#### Dataset
This application was developed with the
A set of sample images has been included in the project as well as their output. The complete dataset can be found [here](http://pascal.inrialpes.fr/data/human/).


#### Note!
.jar and .dylib file for OpenCV should be in the src directory, otherwise there is a risk of getting a java.lang.UnsatisfiedLinkError. See [how to install](https://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html) and [more info](https://github.com/opencv/opencv/tree/master/samples/java/sbt).