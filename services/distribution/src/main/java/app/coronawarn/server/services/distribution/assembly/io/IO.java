

package app.coronawarn.server.services.distribution.assembly.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * A class containing helper functions for general purpose file IO.
 */
public class IO {

  /**
   * The maximum acceptable file size in bytes.
   */
  public static final int MAXIMUM_FILE_SIZE = 16000000;

  private IO() {
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

    try (FileOutputStream outputFileStream = new FileOutputStream(outputFile)) {
      outputFileStream.write(bytes);
    } catch (IOException e) {
      throw new UncheckedIOException("Could not write file " + outputFile, e);
    }
  }
}
