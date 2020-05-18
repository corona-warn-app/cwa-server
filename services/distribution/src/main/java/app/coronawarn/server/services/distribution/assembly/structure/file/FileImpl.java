package app.coronawarn.server.services.distribution.assembly.structure.file;

import app.coronawarn.server.services.distribution.assembly.io.IO;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableImpl;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;

/**
 * Implementation of {@link File} that interfaces with {@link java.io.File Files} on disk.
 */
public class FileImpl extends WritableImpl implements File {

  private byte[] bytes;

  public FileImpl(String name, byte[] bytes) {
    super(name);
    this.bytes = bytes;
  }

  /**
   * Creates a {@link java.io.File} with name {@link Writable#getName} on disk and writes the {@link File#getBytes
   * bytes} of this {@link File} into the {@link java.io.File} to disk.
   */
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

  /**
   * Does nothing.
   */
  @Override
  public void prepare(ImmutableStack<Object> indices) {
  }
}
