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
    Directory decoree = mock(Directory.class);
    Directory decorator = new TestDirectoryDecorator(decoree);

    ImmutableStack<Object> stack = new ImmutableStack<>();
    decorator.prepare(stack);
    verify(decoree).prepare(stack);

    File file = new FileImpl("foo", new byte[0]);
    decorator.addFile(file);
    verify(decoree).addFile(file);

    decorator.getFiles();
    verify(decoree).getFiles();

    Directory directory = new DirectoryImpl("foo");
    decorator.addDirectory(directory);
    verify(decoree).addDirectory(directory);

    decorator.getDirectories();
    verify(decoree).getDirectories();

    decorator.write();
    verify(decoree).write();

    decorator.getName();
    verify(decoree).getName();

    decorator.getParent();
    verify(decoree).getParent();

    decorator.setParent(directory);
    verify(decoree).setParent(directory);

    decorator.getFileOnDisk();
    verify(decoree).getFileOnDisk();
  }

  private static class TestDirectoryDecorator extends DirectoryDecorator {

    protected TestDirectoryDecorator(Directory directory) {
      super(directory);
    }
  }
}
