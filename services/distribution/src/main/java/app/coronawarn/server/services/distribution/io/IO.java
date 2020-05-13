package app.coronawarn.server.services.distribution.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class IO {

  public static byte[] getBytesFromFile(File file) throws IOException {
    return Files.readAllBytes(file.toPath());
  }

  public static void makeFile(File root, String name) {
    File directory = new File(root.getPath() + "/" + name);
    try {
      directory.createNewFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void writeBytesToFile(byte[] bytes, File outputFile) {
    try (FileOutputStream outputFileStream = new FileOutputStream(outputFile)) {
      outputFileStream.write(bytes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
