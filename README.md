# SparkOpenCVFaceDetection-
A simple face detection tool constructed in order to be scalable ad distributed, implemented through Spark and OpenCV


## Current state

- How do we load it to HDFS? Should we load all images file wise in byte format?

- How do we read from? Can we somehow iterate over the files in the HDFS?


## How to run (new approach with HDFS)
- Start nodes
```
$HADOOP_HOME/sbin/hadoop-daemon.sh start namenode
$HADOOP_HOME/sbin/hadoop-daemon.sh start datanode
```

- Create directory /images in HDFS
```
$HADOOP_HOME/bin/hdfs dfs -ls /images
```

- Add images to hdfs/images
```
javac ImagesToHDFS.java -cp $HADOOP_CLASSPATH
java ImagesToHDFS -cp $HADOOP_CLASSPATH
```




## How to run // OLD APPROACH WITH KAFKA

- Start Zookeeper
```
$KAFKA_HOME/bin/zookeeper-server-start.sh $KAFKA_HOME/config/zookeeper.properties
```

- Start kafka
```
$KAFKA_HOME/bin/kafka-server-start.sh $KAFKA_HOME/config/server.properties
```

- Start Spark


- Run Scala script with spark, reading images from topic `images`
!! incomplete

- Run the kafka-picture-producer

In order to streama all images from folder `/data/INRIAPerson/Train/pos` to the topic `images`

go to:
```
/packages/kafka-picture-producer/build/libs
```
run
```
java -jar kafka-picture-producer-0.1.0.jar -imagePath /Users/philipclaesson/ML/DIC/project/data/INRIAPerson/Train/pos --kafka.topic "images"
```

## to do
- get the producer to work
- look into serialization/decoding of images
- get the connection to spark up and running and show that images are streamed to spark.
