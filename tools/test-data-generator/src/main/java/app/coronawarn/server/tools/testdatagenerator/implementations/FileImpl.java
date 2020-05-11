package app.coronawarn.server.tools.testdatagenerator.implementations;

import app.coronawarn.server.tools.testdatagenerator.interfaces.File;
import app.coronawarn.server.tools.testdatagenerator.util.IO;
import java.util.Stack;

public class FileImpl extends WritableImpl implements File {

  private byte[] bytes;

  public FileImpl(String name, byte[] bytes) {
    super(name);
    this.bytes = bytes;
  }

  @Override
  public void write() {
    IO.makeFile(this.getParent().getFileOnDisk(), this.getName());
    IO.writeBytesToFile(this.getBytes(), this.getFileOnDisk());
  }

  @Override
  public byte[] getBytes() {
    return this.bytes;
  }

  @Override
  public void setBytes(byte[] bytes) {
    this.bytes = bytes;
  }

  @Override
  public void prepare(Stack<Object> indices) {}
}
