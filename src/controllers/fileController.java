package controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class fileController {
    public static void saveFile(String message) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String content = "";
        String currentTime = dateFormat.format(new Date());

        if (Files.exists(Path.of("log.txt")))
            content = Files.readString(Path.of("log.txt"));

        PrintWriter out = new PrintWriter("log.txt");
        out.write("%s%n[%s] %s".formatted(content, currentTime, message));
        out.close();
    }
}
