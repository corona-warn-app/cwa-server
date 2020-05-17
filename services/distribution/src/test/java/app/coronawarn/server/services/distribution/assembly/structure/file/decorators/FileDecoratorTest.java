package app.coronawarn.server.services.distribution.assembly.structure.file.decorators;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.file.decorator.FileDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import org.junit.jupiter.api.Test;

public class FileDecoratorTest {

  @Test
  public void checkProxiesAllMethods() {
    File decoratee = mock(File.class);
    File decorator = new TestFileDecorator(decoratee);

    ImmutableStack<Object> stack = new ImmutableStack<>();
    decorator.prepare(stack);
    verify(decoratee).prepare(stack);

    decorator.getBytes();
    verify(decoratee).getBytes();

    byte[] bytes = new byte[0];
    decorator.setBytes(bytes);
    verify(decoratee).setBytes(bytes);

    decorator.write();
    verify(decoratee).write();

    decorator.getName();
    verify(decoratee).getName();

    decorator.getParent();
    verify(decoratee).getParent();

    Directory parent = new DirectoryImpl("foo");
    decorator.setParent(parent);
    verify(decoratee).setParent(parent);

    decorator.getFileOnDisk();
    verify(decoratee).getFileOnDisk();
  }

  private static class TestFileDecorator extends FileDecorator {

    protected TestFileDecorator(File file) {
      super(file);
    }
  }
}
