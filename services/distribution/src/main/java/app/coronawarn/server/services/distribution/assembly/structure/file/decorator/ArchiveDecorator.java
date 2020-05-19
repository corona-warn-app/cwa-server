package app.coronawarn.server.services.distribution.assembly.structure.file.decorator;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.file.Archive;
import java.util.Set;

public abstract class ArchiveDecorator extends FileDecorator implements Archive {

  private final Archive archive;

  protected ArchiveDecorator(Archive archive) {
    super(archive);
    this.archive = archive;
  }

  @Override
  public void addWritable(Writable writable) {
    this.archive.addWritable(writable);
  }

  @Override
  public Set<Writable> getWritables() {
    return this.archive.getWritables();
  }
}
