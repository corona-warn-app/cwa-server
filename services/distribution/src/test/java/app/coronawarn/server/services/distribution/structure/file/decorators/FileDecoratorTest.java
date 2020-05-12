package app.coronawarn.server.services.distribution.structure.file.decorators;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import app.coronawarn.server.services.distribution.structure.directory.Directory;
import app.coronawarn.server.services.distribution.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.structure.file.File;
import app.coronawarn.server.services.distribution.structure.file.FileImpl;
import app.coronawarn.server.services.distribution.structure.file.decorator.FileDecorator;
import java.util.Stack;
import org.junit.jupiter.api.Test;

public class FileDecoratorTest {

  @Test
  public void checkProxiesAllMethods() {
    File decoree = mock(File.class);
    File decorator = new TestFileDecorator(decoree);

    Stack<Object> stack = new Stack<>();
    decorator.prepare(stack);
    verify(decoree).prepare(stack);

    decorator.getBytes();
    verify(decoree).getBytes();

    byte[] bytes = new byte[0];
    decorator.setBytes(bytes);
    verify(decoree).setBytes(bytes);

    decorator.write();
    verify(decoree).write();

    decorator.getName();
    verify(decoree).getName();

    decorator.getParent();
    verify(decoree).getParent();

    Directory parent = new DirectoryImpl("foo");
    decorator.setParent(parent);
    verify(decoree).setParent(parent);

    decorator.getFileOnDisk();
    verify(decoree).getFileOnDisk();
  }

  private class TestFileDecorator extends FileDecorator {

    protected TestFileDecorator(File file) {
      super(file);
    }
  }

}
