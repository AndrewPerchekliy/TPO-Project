package zad2;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Futil {
    public static void processDir(String dirName, String resultFileName) {
        try {
            Path dirPath = Paths.get(dirName);
            Path resultPath = Paths.get(resultFileName);
            Charset inputCharset = Charset.forName("Cp1250");
            Charset outputCharset = StandardCharsets.UTF_8;
            
            try (Stream<Path> paths = Files.walk(dirPath)) {
                String combinedContent = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".txt"))
                    .sorted()
                    .flatMap(path -> {
                        try {
                            return Files.lines(path, inputCharset);
                        } catch (IOException e) {
                            throw new RuntimeException("Error podczas czytania pliku: " + path, e);
                        }
                    })
                    .collect(java.util.stream.Collectors.joining(System.lineSeparator()));
                
                Files.write(resultPath, combinedContent.getBytes(outputCharset));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error podczas przetwarzania katalogu", e);
        }
    }
}

