

package app.coronawarn.server.services.distribution.assembly.structure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;

import java.io.File;
import org.junit.jupiter.api.Test;

class WritableTest {

  private static class TestWritable extends WritableOnDisk {

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
  void checkGetName() {
    String name = "Test";
    WritableOnDisk writable = new TestWritable(name);
    assertThat(writable.getName()).isEqualTo(name);
  }

  @Test
  void checkGetAndSetParent() {
    DirectoryOnDisk parent = new DirectoryOnDisk("Parent");
    WritableOnDisk child = new TestWritable("Child");
    child.setParent(parent);
    assertThat(child.getParent()).isEqualTo(parent);
  }

  @Test
  void checkGetFileOnDiskForRoot() {
    File file = new File("Root");
    DirectoryOnDisk parent = new DirectoryOnDisk(file);
    assertThat(parent.getFileOnDisk()).isEqualTo(file);
  }

  @Test
  void checkGetFileOnDiskRelativeToRoot() {
    File file = new File("Root");
    DirectoryOnDisk parent = new DirectoryOnDisk(file);
    WritableOnDisk child = new TestWritable("Child");
    child.setParent(parent);
    assertThat(child.getFileOnDisk()).isEqualTo(file.toPath().resolve("Child").toFile());
  }

  @Test
  void checkGetFileOnDiskThrowsIfNoParent() {
    assertThat(catchThrowable(new DirectoryOnDisk("Orphan")::getFileOnDisk))
        .isInstanceOf(NullPointerException.class);
  }
}
