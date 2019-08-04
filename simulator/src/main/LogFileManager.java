package main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class LogFileManager {

    private BufferedWriter writer;

    public LogFileManager() throws IOException {
        writer = new BufferedWriter(new FileWriter("./log.txt"));
    }


    public void escribirDatos(String dato) throws IOException {
        long estimatedTime = System.nanoTime() - Main.startTime;
        writer.write("runtime since inicio: "+ (TimeUnit.MILLISECONDS.convert(estimatedTime, TimeUnit.NANOSECONDS)) + "ms\n");
        writer.write(dato + "\n\n");
        writer.flush();
    }

}
