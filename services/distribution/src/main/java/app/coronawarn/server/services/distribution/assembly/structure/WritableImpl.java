package app.coronawarn.server.services.distribution.assembly.structure;

import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import java.util.Objects;

public abstract class WritableImpl implements Writable {

  private String name;
  private Directory parent;
  private java.io.File fileOnDisk;

  protected WritableImpl(String name) {
    this.name = name;
  }

  protected WritableImpl(java.io.File fileOnDisk) {
    this.fileOnDisk = fileOnDisk;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public Directory getParent() {
    return this.parent;
  }

  @Override
  public void setParent(Directory parent) {
    this.parent = parent;
  }

  @Override
  public java.io.File getFileOnDisk() {
    return Objects.requireNonNullElseGet(this.fileOnDisk,
        () -> getParent().getFileOnDisk().toPath().resolve(this.getName()).toFile());
  }
}
