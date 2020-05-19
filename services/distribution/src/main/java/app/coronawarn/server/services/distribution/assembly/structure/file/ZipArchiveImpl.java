package app.coronawarn.server.services.distribution.assembly.structure.file;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritablesContainer;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryImpl;
import java.util.Set;

public class ZipArchiveImpl extends FileImpl implements Archive {

  private Directory tempDirectory = new DirectoryImpl("");

  public ZipArchiveImpl(String name) {
    super(name, new byte[0]);
  }

  @Override
  public void setParent(WritablesContainer parent) {
    super.setParent(parent);
    tempDirectory.setParent(parent);
  }

  @Override
  public void addWritable(Writable writable) {
    this.tempDirectory.addWritable(writable);
  }

  @Override
  public Set<Writable> getWritables() {
    return this.tempDirectory.getWritables();
  }

  @Override
  public void write() {
    // TODO Write temp directory, zip, write zip, remove temp directory
    super.write();
  }
}
