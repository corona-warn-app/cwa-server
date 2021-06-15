

package app.coronawarn.server.services.distribution.assembly.structure.directory.decorator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import org.junit.jupiter.api.Test;

class DirectoryDecoratorTest {

  @Test
  void checkProxiesAllMethods() {
    Directory<WritableOnDisk> decoratee = mock(DirectoryOnDisk.class);
    Directory<WritableOnDisk> decorator = new TestDirectoryDecorator(decoratee);

    ImmutableStack<Object> stack = new ImmutableStack<>();
    decorator.prepare(stack);
    verify(decoratee).prepare(stack);

    Directory<WritableOnDisk> directory = new DirectoryOnDisk("foo");
    decorator.addWritable(directory);
    verify(decoratee).addWritable(directory);

    decorator.getWritables();
    verify(decoratee).getWritables();

    decorator.write();
    verify(decoratee).write();

    decorator.getName();
    verify(decoratee).getName();

    decorator.getParent();
    verify(decoratee).getParent();

    decorator.setParent(directory);
    verify(decoratee).setParent(directory);
  }

  private static class TestDirectoryDecorator extends DirectoryDecorator<WritableOnDisk> {

    protected TestDirectoryDecorator(Directory<WritableOnDisk> directory) {
      super(directory);
    }
  }
}
