

package app.coronawarn.server.services.distribution.assembly.structure.file;


import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.common.shared.util.IoUtils;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;

/**
 * A {@link File} that can be written to disk.
 */
public class FileOnDisk extends WritableOnDisk implements File<WritableOnDisk> {

  private byte[] bytes;

  public FileOnDisk(String name, byte[] bytes) {
    super(name);
    this.bytes = bytes;
  }

  /**
   * Creates a {@link java.io.File} with name {@link Writable#getName} on disk and writes the {@link File#getBytes
   * bytes} of this {@link File} into that {@link java.io.File}.
   */
  @Override
  public void write() {
    IoUtils.makeNewFile(getRoot(), this.getName());
    IoUtils.writeBytesToFile(this.getBytes(), this.getFileOnDisk());
  }

  protected java.io.File getRoot() {
    return ((WritableOnDisk) this.getParent()).getFileOnDisk();
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
    // Method override exists here to comply with the implementation rules for the Writable interface.
  }
}
