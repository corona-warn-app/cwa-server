

package app.coronawarn.server.common.federation.client.download;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import java.util.Objects;
import java.util.Optional;

/**
 * Contains the {@link DiagnosisKeyBatch} and batch tag metadata as served by the federation gateway.
 */
public class BatchDownloadResponse {

  private final DiagnosisKeyBatch diagnosisKeyBatch;
  private final String batchTag;
  private final Optional<String> nextBatchTag;

  /**
   * Creates a FederationGatewayResponse that holds a {@link DiagnosisKeyBatch} and batch tag metadata as served by the
   * federation gateway.
   */
  public BatchDownloadResponse(
      DiagnosisKeyBatch diagnosisKeyBatch, String batchTag, Optional<String> nextBatchTag) {
    this.diagnosisKeyBatch = diagnosisKeyBatch;
    this.batchTag = batchTag;
    this.nextBatchTag = nextBatchTag;
  }

  public DiagnosisKeyBatch getDiagnosisKeyBatch() {
    return diagnosisKeyBatch;
  }

  public String getBatchTag() {
    return batchTag;
  }

  public Optional<String> getNextBatchTag() {
    return nextBatchTag;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BatchDownloadResponse that = (BatchDownloadResponse) o;
    return Objects.equals(diagnosisKeyBatch, that.diagnosisKeyBatch)
        && Objects.equals(batchTag, that.batchTag)
        && Objects.equals(nextBatchTag, that.nextBatchTag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(diagnosisKeyBatch, batchTag, nextBatchTag);
  }
}
