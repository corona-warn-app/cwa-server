package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory;

import static app.coronawarn.server.services.distribution.common.Helpers.getFilePaths;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

class NilDirectoryTest {

  private final Directory<WritableOnDisk> nilDirectory = new NilDirectory();

  @Rule
  private final TemporaryFolder outputFolder = new TemporaryFolder();

  private File outputFile;

  @BeforeEach
  void setupAll() throws IOException {
    outputFolder.create();
    outputFile = outputFolder.newFolder();
  }

  @Test
  void testAddWritableDoesNothing() {
    nilDirectory.addWritable(nilDirectory);
    assertThat(nilDirectory.getWritables()).isEmpty();
  }

  @Test
  void testGetWritablesReturnsEmptySet() {
    assertThat(nilDirectory.getWritables()).isEmpty();
  }

  @Test
  void testWriteDoesNothing() {
    nilDirectory.write();
    Set<String> actualFiles = getFilePaths(outputFile, outputFile.getAbsolutePath());
    assertThat(actualFiles).isEmpty();
  }

  @Test
  void testSetParentDoesNothing() {
    nilDirectory.setParent(null);
    assertThat(nilDirectory.getParent()).isEqualTo(NilDirectory.self);
  }

  @Test
  void testPrepareDoesNothing() {
    nilDirectory.prepare(new ImmutableStack<>());
    nilDirectory.write();
    Set<String> actualFiles = getFilePaths(outputFile, outputFile.getAbsolutePath());
    assertThat(actualFiles).isEmpty();
  }

  @Test
  void testGetNameReturnsEmptyString() {
    assertThat(nilDirectory.getName()).isEmpty();
  }

  @Test
  void testGetParentReturnsSelf() {
    assertThat(nilDirectory.getParent()).isEqualTo(NilDirectory.self);
  }

  @Test
  void testIsFileReturnsFalse() {
    assertThat(nilDirectory.isFile()).isFalse();
  }

  @Test
  void testIsDirectoryReturnsFalse() {
    assertThat(nilDirectory.isDirectory()).isFalse();
  }

  @Test
  void testIsArchiveReturnsFalse() {
    assertThat(nilDirectory.isArchive()).isFalse();
  }
}
