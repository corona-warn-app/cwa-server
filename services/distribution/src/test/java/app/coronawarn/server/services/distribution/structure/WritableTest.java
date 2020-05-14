package app.coronawarn.server.services.distribution.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.coronawarn.server.services.distribution.structure.directory.Directory;
import app.coronawarn.server.services.distribution.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.structure.util.ImmutableStack;
import java.io.File;
import org.junit.jupiter.api.Test;

public class WritableTest {

  private static class TestWritable extends WritableImpl {

    protected TestWritable(String name) {
      super(name);
    }

    @Override
    public void write() {
    }

    @Override
    public void prepare(ImmutableStack<Object> indices) {
    }
  }

  @Test
  public void checkGetName() {
    String name = "Test";
    Writable writable = new TestWritable(name);
    assertEquals(name, writable.getName());
  }

  @Test
  public void checkGetAndSetParent() {
    Directory parent = new DirectoryImpl("Parent");
    Writable child = new TestWritable("Child");
    child.setParent(parent);
    assertEquals(parent, child.getParent());
  }

  @Test
  public void checkGetFileOnDiskForRoot() {
    File file = new File("Root");
    Directory parent = new DirectoryImpl(file);
    assertEquals(file, parent.getFileOnDisk());
  }

  @Test
  public void checkGetFileOnDiskRelativeToRoot() {
    File file = new File("Root");
    Directory parent = new DirectoryImpl(file);
    Writable child = new TestWritable("Child");
    child.setParent(parent);
    assertEquals(file.toPath().resolve("Child").toFile(), child.getFileOnDisk());
  }

  @Test
  public void checkGetFileOnDiskThrowsIfNoParent() {
    Directory orphan = new DirectoryImpl("Orphan");
    assertThrows(NullPointerException.class, orphan::getFileOnDisk);
  }
}
