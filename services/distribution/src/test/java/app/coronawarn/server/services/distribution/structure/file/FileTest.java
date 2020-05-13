package app.coronawarn.server.services.distribution.structure.file;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.coronawarn.server.services.distribution.structure.directory.Directory;
import app.coronawarn.server.services.distribution.structure.directory.DirectoryImpl;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

public class FileTest {

  private byte[] bytes = "World".getBytes();
  private File file;

  @Rule
  private TemporaryFolder outputFolder = new TemporaryFolder();

  @BeforeEach
  public void setup() throws IOException {
    outputFolder.create();
    file = new FileImpl("Hello", bytes);
  }

  @Test
  public void checkGetBytes() {
    assertEquals(bytes, file.getBytes());
  }

  @Test
  public void checkSetBytes() {
    byte[] bytes = "Goodbye".getBytes();
    file.setBytes(bytes);
    assertEquals(bytes, file.getBytes());
  }

  @Test
  public void checkWriteThrowsWithoutParent() {
    assertThrows(NullPointerException.class, file::write);
  }

  @Test
  public void checkWrite() throws IOException {
    java.io.File outputFile = outputFolder.newFolder();
    Directory directory = new DirectoryImpl(outputFile);

    directory.addFile(file);
    directory.write();

    byte[] writtenBytes = Files.readAllBytes(file.getFileOnDisk().toPath());

    assertArrayEquals(bytes, writtenBytes);
    assertEquals(1, outputFile.listFiles().length);
  }

}
