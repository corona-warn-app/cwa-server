package app.coronawarn.server.services.distribution.assembly.structure.directory.decorators;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.DirectoryDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileImpl;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import org.junit.jupiter.api.Test;

public class DirectoryDecoratorTest {

  @Test
  public void checkProxiesAllMethods() {
    Directory decoratee = mock(Directory.class);
    Directory decorator = new TestDirectoryDecorator(decoratee);

    ImmutableStack<Object> stack = new ImmutableStack<>();
    decorator.prepare(stack);
    verify(decoratee).prepare(stack);

    File file = new FileImpl("foo", new byte[0]);
    decorator.addFile(file);
    verify(decoratee).addFile(file);

    decorator.getFiles();
    verify(decoratee).getFiles();

    Directory directory = new DirectoryImpl("foo");
    decorator.addDirectory(directory);
    verify(decoratee).addDirectory(directory);

    decorator.getDirectories();
    verify(decoratee).getDirectories();

    decorator.write();
    verify(decoratee).write();

    decorator.getName();
    verify(decoratee).getName();

    decorator.getParent();
    verify(decoratee).getParent();

    decorator.setParent(directory);
    verify(decoratee).setParent(directory);

    decorator.getFileOnDisk();
    verify(decoratee).getFileOnDisk();
  }

  private static class TestDirectoryDecorator extends DirectoryDecorator {

    protected TestDirectoryDecorator(Directory directory) {
      super(directory);
    }
  }
}
