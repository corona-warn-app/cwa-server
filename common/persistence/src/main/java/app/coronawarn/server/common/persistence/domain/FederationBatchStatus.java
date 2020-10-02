

package app.coronawarn.server.common.persistence.domain;

public enum FederationBatchStatus {
  /**
   * The corresponding batch has not been processed yet.
   */
  UNPROCESSED,
  /**
   * The corresponding batch has been processed.
   */
  PROCESSED,
  /**
   * An error occurred while processing the batch.
   */
  ERROR,
  /**
   * Processing a batch failed for the second time and will not be attempted again.
   */
  ERROR_WONT_RETRY
}
