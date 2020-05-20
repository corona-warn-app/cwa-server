package app.coronawarn.server.services.distribution.assembly.structure.directory.decorators;

import static org.mockito.Mockito.mock;

import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.IndexDirectoryDecorator;
import org.junit.jupiter.api.Test;

public class IndexDirectoryDecoratorTest {

  @Test
  public void checkProxiesAllMethods() {
    IndexDirectory<?, WritableOnDisk> decoratee = mock(IndexDirectoryOnDisk.class);
    IndexDirectory<?, WritableOnDisk> decorator = new TestIndexDirectoryDecorator<>(decoratee);

    // TODO
  }

  private static class TestIndexDirectoryDecorator<T> extends IndexDirectoryDecorator<T, WritableOnDisk> {

    protected TestIndexDirectoryDecorator(IndexDirectory<T, WritableOnDisk> directory) {
      super(directory);
    }
  }
}