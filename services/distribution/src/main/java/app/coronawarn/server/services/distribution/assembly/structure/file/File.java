

package app.coronawarn.server.services.distribution.assembly.structure.file;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;

/**
 * A {@link Writable} that contains some bytes.
 *
 * @param <W> The specific type of {@link Writable} that this {@link File} can be a child of.
 */
public interface File<W extends Writable<W>> extends Writable<W> {

  /**
   * Returns the bytes contained by this {@link File}.
   */
  byte[] getBytes();

  /**
   * Sets the bytes to be contained by this {@link File}.
   */
  void setBytes(byte[] bytes);
}
