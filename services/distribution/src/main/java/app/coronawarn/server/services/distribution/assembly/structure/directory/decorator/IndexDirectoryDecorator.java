package app.coronawarn.server.services.distribution.assembly.structure.directory.decorator;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.util.functional.Formatter;
import app.coronawarn.server.services.distribution.assembly.structure.util.functional.WritableFunction;
import java.util.Set;

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
