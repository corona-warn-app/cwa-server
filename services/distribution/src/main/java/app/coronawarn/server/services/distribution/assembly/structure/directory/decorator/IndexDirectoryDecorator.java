

package app.coronawarn.server.services.distribution.assembly.structure.directory.decorator;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.common.shared.functional.Formatter;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.util.functional.WritableFunction;
import java.util.Set;

/**
 * Decorates an {@link IndexDirectory} (e.g. to modify its files, subdirectories, etc.) on {@link Writable#prepare}.
 * This class proxies all function calls to the {@link IndexDirectory} it decorates.
 */
public abstract class IndexDirectoryDecorator<T, W extends Writable<W>> extends DirectoryDecorator<W> implements
    IndexDirectory<T, W> {

  private final IndexDirectory<T, W> directory;

  protected IndexDirectoryDecorator(IndexDirectory<T, W> directory) {
    super(directory);
    this.directory = directory;
  }

  @Override
  public void addWritableToAll(WritableFunction<W> writableFunction) {
    this.directory.addWritableToAll(writableFunction);
  }

  @Override
  public Set<T> getIndex(ImmutableStack<Object> indices) {
    return this.directory.getIndex(indices);
  }

  @Override
  public Formatter<T> getIndexFormatter() {
    return this.directory.getIndexFormatter();
  }
}
