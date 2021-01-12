package app.coronawarn.server.services.distribution.assembly.structure.file;

import static app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDiskWithChecksum.buildChecksumPathForFile;
import static java.nio.file.Files.readString;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileOnDiskWithChecksumTest {

  private final byte[] bytes = "World".getBytes();
  private FileOnDiskWithChecksum file;

  @TempDir
  java.io.File outputFolder;

  @Test
  void checkChecksum() throws IOException {
    file = new FileOnDiskWithChecksum("Hello", bytes);
    file.setParent(new DirectoryOnDisk(outputFolder));
    file.write();
    String checksum = readString(buildChecksumPathForFile(file.getFileOnDisk().toPath())).trim();

    // NOTE: If this value changes, please adapt
    // https://github.com/corona-warn-app/cwa-server/blob/HEAD/docs/DISTRIBUTION.md#cwa-hash
    assertThat(checksum).isEqualTo("65584eac1cb9fd270adb3a733be69c3e");
  }

}
