package controllers;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class fileController {
    public static void saveFile(String message) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String content = "";
        String separator = "";
        String currentTime = sdf.format(new Date());
        if (Files.exists(Path.of("log.txt"))) {
            content = Files.readString(Path.of("log.txt"));
            separator = "\n";
        }
        PrintWriter out = new PrintWriter("log.txt");
        out.write(content + separator + "[" + currentTime + "] " + message);
        out.close();
    }
}
