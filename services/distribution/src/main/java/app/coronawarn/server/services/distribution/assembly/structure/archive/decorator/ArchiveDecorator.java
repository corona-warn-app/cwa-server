

package app.coronawarn.server.services.distribution.assembly.structure.archive.decorator;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.DirectoryDecorator;

/**
 * Decorates a {@link Directory} (e.g. to modify its files, subdirectories, etc.) on {@link Writable#prepare}. This
 * class proxies all function calls to the {@link Directory} it decorates.
 */
public abstract class ArchiveDecorator<W extends Writable<W>> extends DirectoryDecorator<W> implements Archive<W> {

  private final Archive<W> archive;

  protected ArchiveDecorator(Archive<W> archive) {
    super(archive);
    this.archive = archive;
  }

  @Override
  public byte[] getBytes() {
    return this.archive.getBytes();
  }

  @Override
  public void setBytes(byte[] bytes) {
    this.archive.setBytes(bytes);
  }
}
