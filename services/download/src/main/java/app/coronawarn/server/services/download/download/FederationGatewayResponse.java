package app.coronawarn.server.services.download.download;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Contains the {@link DiagnosisKeyBatch} and batch tag metadata as served by the federation gateway.
 */
public class FederationGatewayResponse {

  private final DiagnosisKeyBatch diagnosisKeyBatch;
  private final String batchTag;
  private final Optional<String> nextBatchTag;
  private final LocalDate date;

  /**
   * Creates a DiagnosisKeyBatchContainer.
   */
  public FederationGatewayResponse(
      DiagnosisKeyBatch diagnosisKeyBatch, String batchTag, Optional<String> nextBatchTag, LocalDate date) {
    this.diagnosisKeyBatch = diagnosisKeyBatch;
    this.batchTag = batchTag;
    this.nextBatchTag = nextBatchTag;
    this.date = date;
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

  public LocalDate getDate() {
    return date;
  }
}
