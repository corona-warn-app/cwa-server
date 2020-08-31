package app.coronawarn.server.services.download.download;

import static org.apache.commons.lang3.StringUtils.isBlank;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Contains the {@link DiagnosisKeyBatch} and batch tag metadata as served by the federation gateway.
 * TODO consider "FederationGatewayResponse" as name
 */
public class DiagnosisKeyBatchContainer {

  private final DiagnosisKeyBatch diagnosisKeyBatch;
  private final String batchTag;
  private final Optional<String> nextBatchTag;
  private final LocalDate date;

  /**
   * Creates a DiagnosisKeyBatchContainer.
   */
  public DiagnosisKeyBatchContainer(
      DiagnosisKeyBatch diagnosisKeyBatch, String batchTag, String nextBatchTag, LocalDate date) {
    this.diagnosisKeyBatch = diagnosisKeyBatch;
    this.batchTag = batchTag;
    this.nextBatchTag = !isBlank(nextBatchTag) ? Optional.of(nextBatchTag) : Optional.empty();
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
