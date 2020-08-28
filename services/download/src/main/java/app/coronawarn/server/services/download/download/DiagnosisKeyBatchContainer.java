package app.coronawarn.server.services.download.download;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;

/**
 * Contains the {@link DiagnosisKeyBatch} and batch tag metadata as served by the federation gateway.
 */
public class DiagnosisKeyBatchContainer {

  private final DiagnosisKeyBatch diagnosisKeyBatch;
  private final String batchTag;
  private final String nextBatchTag;

  /**
   * Creates a DiagnosisKeyBatchContainer.
   */
  public DiagnosisKeyBatchContainer(DiagnosisKeyBatch diagnosisKeyBatch, String batchTag, String nextBatchTag) {
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

  public String getNextBatchTag() {
    return nextBatchTag;
  }
}
