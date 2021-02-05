

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
   *
   * @param signatureFileName the signature to search
   * @return the file containing the specified signature
   */
  File<W> getSignatureFile(String signatureFileName);

  /**
   * Returns the bytes that shall be signed.
   *
   * @return bite array
   */
  byte[] getBytesToSign();

  /**
   * Returns the index number of the current batch.
   *
   * @return index of current batch
   */
  int getBatchNum();

  /**
   * Returns the total size of the batch.
   *
   * @return size of current batch
   */
  int getBatchSize();
}
