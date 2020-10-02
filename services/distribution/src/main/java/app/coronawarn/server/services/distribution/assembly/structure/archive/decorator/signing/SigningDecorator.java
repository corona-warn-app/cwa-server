

package app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;

/**
 * A decorator that can sign an array of bytes and write the signature into a file.
 */
public interface SigningDecorator<W extends Writable<W>> extends Archive<W> {

  /**
   * Returns the file containing the signature.
   */
  File<W> getSignatureFile(String signatureFileName);

  /**
   * Returns the bytes that shall be signed.
   */
  byte[] getBytesToSign();

  /**
   * Returns the index number of the current batch.
   */
  int getBatchNum();

  /**
   * Returns the total size of the batch.
   */
  int getBatchSize();
}
