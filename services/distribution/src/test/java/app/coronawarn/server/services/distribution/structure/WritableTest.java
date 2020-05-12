package app.coronawarn.server.services.distribution.structure;

import app.coronawarn.server.services.distribution.structure.directory.Directory;
import app.coronawarn.server.services.distribution.structure.directory.DirectoryImpl;
import java.io.File;
import java.util.Stack;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WritableTest {

  private static class WritableTestImpl extends WritableImpl {

    protected WritableTestImpl(String name) {
      super(name);
    }

    protected WritableTestImpl(File fileOnDisk) {
      super(fileOnDisk);
    }

    @Override
    public void write() {
    }

    @Override
    public void prepare(Stack<Object> indices) {
    }
  }

  @Test
  public void checkName() {
    String name = "Test";
    Writable writable = new WritableTestImpl(name);
    assertEquals(name, writable.getName());
  }

  @Test
  public void checkParent() {
    Directory parent = new DirectoryImpl("Parent");
    Writable child = new WritableTestImpl("Child");
    child.setParent(parent);
    assertEquals(parent, child.getParent());
  }

  @Test
  public void checkFileOnDiskForRoot() {
    File file = new File("Root");
    Directory parent = new DirectoryImpl(file);
    assertEquals(file, parent.getFileOnDisk());
  }

  @Test
  public void checkFileOnDiskRelativeToRoot() {
    File file = new File("Root");
    Directory parent = new DirectoryImpl(file);
    Writable child = new WritableTestImpl("Child");
    child.setParent(parent);
    assertEquals(file.toPath().resolve("Child").toFile(), child.getFileOnDisk());
  }

  @Test
  public void checkFileOnDiskExceptionIsOrphan() {
    Directory orphan = new DirectoryImpl("Orphan");
    assertThrows(NullPointerException.class, orphan::getFileOnDisk);
  }
}
