package zad1;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Futil {

    public static void processDir(String dirName, String resultFileName) {
        Path startDir = Paths.get(dirName);
        Path outFile = Paths.get(resultFileName);

        Charset inCharset = Charset.forName("Cp1250");
        Charset outCharset = Charset.forName("UTF-8");

        try (FileChannel outChannel = FileChannel.open(outFile,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {

            Files.walkFileTree(startDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (Files.isRegularFile(file)) {

                        try (FileChannel inChannel = FileChannel.open(file, StandardOpenOption.READ)) {
                            long fileSize = inChannel.size();

                            if (fileSize > 0) {
                                ByteBuffer inBuffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);

                                CharBuffer charBuffer = inCharset.decode(inBuffer);
                                ByteBuffer outBuffer = outCharset.encode(charBuffer);

                                while (outBuffer.hasRemaining()) {
                                    outChannel.write(outBuffer);
                                }
                            }
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    System.err.println("Cannot access file: " + file.toString());
                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (IOException e) {
            System.err.println("Error operacji na pliku wynikowym: " + e.getMessage());
        }
    }
}