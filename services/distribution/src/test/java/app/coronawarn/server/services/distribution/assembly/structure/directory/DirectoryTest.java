

package app.coronawarn.server.services.distribution.assembly.structure.directory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import java.io.IOException;
import java.util.Set;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

class DirectoryTest {

  private java.io.File outputDir = new java.io.File("test");
  private Directory<WritableOnDisk> parentDirectory;
  private File<WritableOnDisk> childFile;

  @Rule
  private TemporaryFolder outputFolder = new TemporaryFolder();

  @BeforeEach
  public void setup() throws IOException {
    outputFolder.create();
    outputDir = outputFolder.newFolder();
    parentDirectory = new DirectoryOnDisk(outputDir);
    childFile = new FileOnDisk("Child", new byte[0]);
  }

  @Test
  void checkWritablesInDirectory() {
    parentDirectory.addWritable(childFile);
    assertThat(parentDirectory.getWritables()).isEqualTo(Set.of(childFile));
  }

  @Test
  void checkParentOfWritablesInDirectory() {
    parentDirectory.addWritable(childFile);
    assertThat(childFile.getParent()).isEqualTo(parentDirectory);
  }

  @Test
  void checkPrepareDelegatesToWritables() {
    File<WritableOnDisk> spyChildFile = spy(childFile);
    ImmutableStack<Object> expectedStack = new ImmutableStack<>();

    parentDirectory.addWritable(spyChildFile);
    parentDirectory.prepare(expectedStack);

    verify(spyChildFile).prepare(expectedStack);
  }

  @Test
  void checkWriteThrowsWithoutParent() {
    assertThat(catchThrowable(new DirectoryOnDisk("")::write))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void checkWriteWritesOwnDirectory() {
    class MockFile extends java.io.File {

      public MockFile() {
        super(outputDir.getPath());
      }
    }

    java.io.File mockOutputDirectory = spy(new MockFile());
    parentDirectory = new DirectoryOnDisk(mockOutputDirectory);

    parentDirectory.write();
    verify(mockOutputDirectory).mkdirs();
  }

  @Test
  void checkWriteDelegatesToWritables() {
    File<WritableOnDisk> spyChildFile = spy(childFile);

    parentDirectory.addWritable(spyChildFile);
    parentDirectory.write();

    verify(spyChildFile).write();
  }
}
