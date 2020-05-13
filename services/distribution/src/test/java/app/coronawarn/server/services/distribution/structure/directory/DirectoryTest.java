package app.coronawarn.server.services.distribution.structure.directory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import app.coronawarn.server.services.distribution.structure.file.File;
import app.coronawarn.server.services.distribution.structure.file.FileImpl;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Stack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class DirectoryTest {

  private java.io.File outputDir = new java.io.File("test");
  private Directory parentDirectory;
  private Directory childDirectory;
  private File childFile;

  @TempDir
  Path tempPath;

  @BeforeEach
  public void setup() throws IOException {
    outputDir = tempPath.toFile();
    parentDirectory = new DirectoryImpl(outputDir);
    childDirectory = new DirectoryImpl("Child");
    childFile = new FileImpl("Child", new byte[0]);
  }

  @Test
  public void checkFilesInDirectory() {
    parentDirectory.addFile(childFile);
    assertEquals(List.of(childFile), parentDirectory.getFiles());
  }

  @Test
  public void checkParentOfFilesInDirectory() {
    parentDirectory.addFile(childFile);
    assertEquals(parentDirectory, childFile.getParent());
  }

  @Test
  public void checkDirectoriesInDirectory() {
    parentDirectory.addDirectory(childDirectory);
    assertEquals(List.of(childDirectory), parentDirectory.getDirectories());
  }

  @Test
  public void checkParentOfDirectoriesInDirectory() {
    parentDirectory.addDirectory(childDirectory);
    assertEquals(parentDirectory, childDirectory.getParent());
  }

  @Test
  public void checkPrepareDelegatesToFiles() {
    File spyChildFile = spy(childFile);
    Stack<Object> expectedStack = new Stack<>();

    parentDirectory.addFile(spyChildFile);
    parentDirectory.prepare(expectedStack);

    verify(spyChildFile).prepare(expectedStack);
  }

  @Test
  public void checkPrepareDelegatesToDirectories() {
    Directory spyChildDirectory = spy(childDirectory);
    Stack<Object> expectedStack = new Stack<>();

    parentDirectory.addDirectory(spyChildDirectory);
    parentDirectory.prepare(expectedStack);

    verify(spyChildDirectory).prepare(expectedStack);
  }

  @Test
  public void checkWriteThrowsWithoutParent() {
    assertThrows(NullPointerException.class, new DirectoryImpl("")::write);
  }

  @Test
  public void checkWriteWritesOwnDirectory() {
    class MockFile extends java.io.File {

      public MockFile() {
        super(outputDir.getPath());
      }
    }

    java.io.File mockOutputDirectory = spy(new MockFile());
    parentDirectory = new DirectoryImpl(mockOutputDirectory);

    parentDirectory.write();
    verify(mockOutputDirectory).mkdirs();
  }

  @Test
  public void checkWriteDelegatesToFiles() {
    File spyChildFile = spy(childFile);

    parentDirectory.addFile(spyChildFile);
    parentDirectory.write();

    verify(spyChildFile).write();
  }

  @Test
  public void checkWriteDelegatesToDirectories() {
    Directory spyChildDirectory = spy(childDirectory);

    parentDirectory.addDirectory(spyChildDirectory);
    parentDirectory.write();

    verify(spyChildDirectory).write();
  }
}
