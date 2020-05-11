package app.coronawarn.server.tools.testdatagenerator.implementations;

import app.coronawarn.server.tools.testdatagenerator.interfaces.Directory;
import app.coronawarn.server.tools.testdatagenerator.interfaces.Writable;
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
