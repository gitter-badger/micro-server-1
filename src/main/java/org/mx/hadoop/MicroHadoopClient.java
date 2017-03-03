package org.mx.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.*;

public class MicroHadoopClient {

    public static void main(String[] args) throws IOException {
        System.setProperty("hadoop.home.dir", "/opt/mxdeploy/hadoop");
        //System.setProperty("java.library.path","/opt/mxdeploy/hadoop/lib");
        Configuration conf = new Configuration();
        conf.addResource(new Path("/opt/mxdeploy/hadoop/etc/hadoop/core-site.xml"));
        conf.addResource(new Path("/opt/mxdeploy/hadoop/etc/hadoop/hdfs-site.xml"));
        conf.addResource(new Path("/opt/mxdeploy/hadoop/etc/hadoop/mapred-site.xml.template"));

//        String hdfsPath = "hdfs://localhost:9870";
//        conf.set("fs.default.name", hdfsPath);

        String file = "input/capacity-scheduler.xml";
        FileSystem fileSystem = FileSystem.get(conf);

        Path path = new Path(file);
        if (!fileSystem.exists(path)) {
            System.out.println("File " + file + " does not exists");
            return;
        }

        FSDataInputStream in = fileSystem.open(path);

        String filename = file.substring(file.lastIndexOf('/') + 1,
                file.length());

        OutputStream out = new BufferedOutputStream(new FileOutputStream(
                new File(filename)));

        byte[] b = new byte[1024];
        int numBytes = 0;
        while ((numBytes = in.read(b)) > 0) {
            out.write(b, 0, numBytes);
        }

        in.close();
        out.close();
        fileSystem.close();
    }
}