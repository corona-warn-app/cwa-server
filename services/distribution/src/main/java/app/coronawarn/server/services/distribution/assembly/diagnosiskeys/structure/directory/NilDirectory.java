package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.util.Collections;
import java.util.Set;

public final class NilDirectory implements Directory<WritableOnDisk> {

  static final NilDirectory self = new NilDirectory();

  @Override
  public void addWritable(Writable<WritableOnDisk> writable) {
  }

  @Override
  public Set<Writable<WritableOnDisk>> getWritables() {
    return Collections.emptySet();
  }

  @Override
  public void write() {
    //do nothing
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public Directory<WritableOnDisk> getParent() {
    return self;
  }

  @Override
  public void setParent(Directory<WritableOnDisk> parent) {
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    //do nothing
  }

  @Override
  public boolean isFile() {
    return false;
  }

  @Override
  public boolean isDirectory() {
    return false;
  }

  @Override
  public boolean isArchive() {
    return false;
  }
}
