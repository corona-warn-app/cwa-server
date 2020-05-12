package app.coronawarn.server.services.distribution.structure.file.decorator;

import app.coronawarn.server.services.distribution.structure.directory.Directory;
import app.coronawarn.server.services.distribution.structure.file.File;
import java.util.Stack;

public abstract class FileDecorator implements File {

  private final File file;

  protected FileDecorator(File file) {
    this.file = file;
  }

  @Override
  public void prepare(Stack<Object> indices) {
    this.file.prepare(indices);
  }

  @Override
  public byte[] getBytes() {
    return this.file.getBytes();
  }

  @Override
  public void setBytes(byte[] bytes) {
    this.file.setBytes(bytes);
  }

  @Override
  public void write() {
    this.file.write();
  }

  @Override
  public String getName() {
    return this.file.getName();
  }

  @Override
  public Directory getParent() {
    return this.file.getParent();
  }

  @Override
  public void setParent(Directory parent) {
    this.file.setParent(parent);
  }

  @Override
  public java.io.File getFileOnDisk() {
    return this.file.getFileOnDisk();
  }
}
