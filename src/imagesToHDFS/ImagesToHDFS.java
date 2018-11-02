package src.imagesToHDFS;


import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.File;
import java.io.IOException;


public class ImagesToHDFS {
    public static void main(String[] args) throws IOException {
        Configuration configuration = new Configuration();
        FileSystem fs = FileSystem.get(configuration);

        String filename = "person_001.png";

        // Read the file first from local Disk
        String originalFile = "./data/INRIAPerson/Test/pos/" + filename;
        byte[] imageFileInBytes = FileUtils.readFileToByteArray(new File(originalFile));

        // Write the image to HDFS
        String destinationPath = "$HADOOP_HOME/bin/hdfs/images/" + filename;
        FSDataOutputStream outputStream = fs.create(new Path(destinationPath));
        outputStream.write(imageFileInBytes);
        outputStream.close();

        // Read it back from HDFS + Write to another location local FS
        String localFileSystemDestinationPath = "./data/testout/" + filename;
        FSDataOutputStream localOutputStream = fs.create(new Path(localFileSystemDestinationPath));

        FSDataInputStream inputStream = fs.open(new Path(destinationPath));
        byte[] buffer = new byte[1024];
        while(inputStream.read(buffer) != -1) {
            localOutputStream.write(buffer);
        }

        inputStream.close();
        localOutputStream.close();
    }
}