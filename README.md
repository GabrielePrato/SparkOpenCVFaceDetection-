# SparkOpenCVFaceDetection-
A simple face detection tool constructed in order to be scalable ad distributed, implemented through Spark and OpenCV


## Current state

### kafka-image-producer
the producer has been built and us runnable according to below instructions. It creates a topic "images" but when consuming from images it is empty. We need to try to look closer into this, also in order to understand how images are serialized.


### Decoding of the streamed images
In order to set up the stream we need to supply a decoder for the streamed images which complies with how the images are serialized in the producer. We should 1. get a producer running and 2. understand which serialization is used.


## How to run

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
java -jar kafka-picture-producer-0.1.0.jar --kafka.topic "images" --imagePath "../../data/INRIAPerson/Train/pos"
```

## to do
- get the producer to work
- look into serialization/decoding of images
- get the connection to spark up and running and show that images are streamed to spark.
