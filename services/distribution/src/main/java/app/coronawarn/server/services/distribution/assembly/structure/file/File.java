package app.coronawarn.server.services.distribution.assembly.structure.file;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;

/**
 * A {@link Writable} containing some bytes.
 */
public interface File extends Writable {

  /**
   * Returns the bytes contained by this {@link File}.
   */
  byte[] getBytes();

  /**
   * Sets the bytes to be contained by this {@link File}.
   */
  void setBytes(byte[] bytes);
}
