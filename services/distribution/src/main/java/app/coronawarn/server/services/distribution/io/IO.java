package app.coronawarn.server.services.distribution.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IO {

  private static final Logger logger = LoggerFactory.getLogger(IO.class);

  public static byte[] getBytesFromFile(File file) throws IOException {
    return Files.readAllBytes(file.toPath());
  }

  public static void makeFile(File root, String name) {
    File directory = new File(root, name);
    try {
      directory.createNewFile();
    } catch (IOException e) {
      logger.error("Failed to create file: {}", name, e);
      throw new RuntimeException(e);
    }
  }

  public static void writeBytesToFile(byte[] bytes, File outputFile) {
    try (FileOutputStream outputFileStream = new FileOutputStream(outputFile)) {
      outputFileStream.write(bytes);
    } catch (IOException e) {
      logger.error("Write operation failed.", e);
      throw new RuntimeException(e);
    }
  }
}
