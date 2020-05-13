package app.coronawarn.server.services.distribution.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class containing helper functions for general purpose file IO.
 */
public class IO {

  private static final Logger logger = LoggerFactory.getLogger(IO.class);

  /**
   * Creates a new file on disk.
   *
   * @param root The parent file.
   * @param name The name of the new file.
   */
  public static void makeFile(File root, String name) {
    File directory = new File(root, name);
    try {
      directory.createNewFile();
    } catch (IOException e) {
      logger.error("Failed to create file: {}", name, e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Reads the contents of a file as {@code byte[]}.
   *
   * @param file The file to read.
   * @return The contents of the file as {@code byte[]}.
   */
  public static byte[] getBytesFromFile(File file) {
    try {
      return Files.readAllBytes(file.toPath());
    } catch (IOException e) {
      logger.error("Read operation failed.", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Writes bytes into a file.
   *
   * @param bytes      The content to write
   * @param outputFile The file to write the content into.
   */
  public static void writeBytesToFile(byte[] bytes, File outputFile) {
    try (FileOutputStream outputFileStream = new FileOutputStream(outputFile)) {
      outputFileStream.write(bytes);
    } catch (IOException e) {
      logger.error("Write operation failed.", e);
      throw new RuntimeException(e);
    }
  }
}
