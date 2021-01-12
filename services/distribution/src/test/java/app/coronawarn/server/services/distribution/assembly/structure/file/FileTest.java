package app.coronawarn.server.services.distribution.assembly.structure.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileTest {

  private final byte[] bytes = "World".getBytes();
  private FileOnDisk file;

  @TempDir
  java.io.File outputFile;
  
  @BeforeEach
  public void setup() throws IOException {
    file = new FileOnDisk("Hello", bytes);
  }

  @Test
  void checkGetBytes() {
    assertThat(file.getBytes()).isEqualTo(bytes);
  }

  @Test
  void checkSetBytes() {
    byte[] bytes = "Goodbye".getBytes();
    file.setBytes(bytes);
    assertThat(file.getBytes()).isEqualTo(bytes);
  }

  @Test
  void checkWriteThrowsWithoutParent() {
    assertThat(catchThrowable(file::write)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void checkWrite() throws IOException {
    
    Directory<WritableOnDisk> directory = new DirectoryOnDisk(outputFile);

    directory.addWritable(file);
    directory.write();

    byte[] writtenBytes = Files.readAllBytes(file.getFileOnDisk().toPath());

    assertThat(writtenBytes).isEqualTo(bytes);
    assertThat(outputFile.listFiles()).isNotNull().hasSize(1);
  }

}
