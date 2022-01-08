package controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class fileController {
    public static void saveFile(String message) throws IOException {
        // create new date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String content = "";
        // Get current time
        String currentTime = dateFormat.format(new Date());

        // If log.txt read it into content
        if (Files.exists(Path.of("log.txt")))
            content = Files.readString(Path.of("log.txt"));

        // Create new file log.txt to replace the old one
        PrintWriter out = new PrintWriter("log.txt");
        // Write changes
        out.write("%s%n[%s] %s".formatted(content, currentTime, message));
        // Close the writer
        out.close();
    }
}
