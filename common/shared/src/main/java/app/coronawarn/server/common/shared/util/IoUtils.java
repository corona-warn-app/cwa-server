package app.coronawarn.server.common.shared.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class containing helper functions for general purpose file IO.
 */
public class IoUtils {

  private static final Logger logger = LoggerFactory.getLogger(IoUtils.class);
  /**
   * The maximum acceptable file size in bytes.
   */
  public static final int MAXIMUM_FILE_SIZE = 16000000;

  public static final int MB_FAKTOR = 1000 * 1000;

  private IoUtils() {
  }

  /**
   * Check whether a directory contains a filename in it.
   *
   * @param parent   - directory
   * @param fileName - file name
   * @return - {@code true} if file exists in directory, {@code false} otherwise
   */
  public static boolean fileExistsInDirectory(File parent, String fileName) {
    return Arrays.stream(Objects.requireNonNull(parent.listFiles()))
        .map(File::getName)
        .anyMatch(name -> name.equalsIgnoreCase(fileName));
  }

  /**
   * Create a file on the disk if it does not already exist.
   *
   * @param root The parent file.
   * @param name The name of the new file.
   */
  public static void makeNewFile(File root, String name) {
    File directory = new File(root, name);
    try {
      if (!directory.createNewFile()) {
        throw new IOException("Could not create " + name + ", file already exists");
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to create file: " + name, e);
    }
  }

  /**
   * Writes bytes into a file. If the resulting file would exceed the specified maximum file size, it is not written but
   * removed instead.
   *
   * @param bytes      The content to write
   * @param outputFile The file to write the content into.
   */
  public static void writeBytesToFile(byte[] bytes, File outputFile) {
    if (bytes.length > MAXIMUM_FILE_SIZE) {
      String fileName = outputFile.getName();
      throw new UncheckedIOException(
          new IOException(
              "File size of " + bytes.length + " bytes exceeds the maximum file size. Deleting" + fileName));
    }

    if (bytes.length >= MAXIMUM_FILE_SIZE * 0.75 && bytes.length <= MAXIMUM_FILE_SIZE * 0.9) {
      logger.warn("File '{}' ({} MB) reaches 75% of {} MB limit!", outputFile.getAbsolutePath(),
          bytes.length / MB_FAKTOR, MAXIMUM_FILE_SIZE / MB_FAKTOR);
    } else if (bytes.length >= MAXIMUM_FILE_SIZE * 0.9) {
      logger.error("File '{}' ({} MB) reaches 90% of {} MB limit!", outputFile.getAbsolutePath(),
          bytes.length / MB_FAKTOR, MAXIMUM_FILE_SIZE / MB_FAKTOR);
    }

    try (FileOutputStream outputFileStream = new FileOutputStream(outputFile)) {
      outputFileStream.write(bytes);
    } catch (IOException e) {
      throw new UncheckedIOException("Could not write file " + outputFile, e);
    }
  }
}
