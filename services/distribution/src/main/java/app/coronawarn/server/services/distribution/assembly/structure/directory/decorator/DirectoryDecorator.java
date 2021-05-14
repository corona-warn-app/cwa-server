

package app.coronawarn.server.services.distribution.assembly.structure.directory.decorator;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import java.util.Set;

/**
 * Decorates a {@link Directory} (e.g. to modify its files, subdirectories, etc.) on {@link Writable#prepare}. This
 * class proxies all function calls to the {@link Directory} it decorates.
 */
public abstract class DirectoryDecorator<W extends Writable<W>> implements Directory<W> {

  private final Directory<W> directory;

  protected DirectoryDecorator(Directory<W> directory) {
    this.directory = directory;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.directory.prepare(indices);
  }

  @Override
  public void addWritable(Writable<W> writable) {
    this.directory.addWritable(writable);
  }

  @Override
  public Set<Writable<W>> getWritables() {
    return this.directory.getWritables();
  }

  @Override
  public void write() {
    this.directory.write();
  }

  @Override
  public String getName() {
    return this.directory.getName();
  }

  @Override
  public Directory<W> getParent() {
    return this.directory.getParent();
  }

  @Override
  public void setParent(Directory<W> parent) {
    this.directory.setParent(parent);
  }

  @Override
  public boolean isFile() {
    return this.directory.isFile();
  }

  @Override
  public boolean isDirectory() {
    return this.directory.isDirectory();
  }

  @Override
  public boolean isArchive() {
    return this.directory.isArchive();
  }
}
