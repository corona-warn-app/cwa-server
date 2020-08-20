package app.coronawarn.server.services.federation.download.download;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;

public class DiagnosisKeyBatchContainer {

  private final DiagnosisKeyBatch diagnosisKeyBatch;
  private String batchTag;
  private String nextBatchTag;

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

  public void setBatchTag(String batchTag) {
    this.batchTag = batchTag;
  }

  public String getNextBatchTag() {
    return nextBatchTag;
  }

  public void setNextBatchTag(String nextBatchTag) {
    this.nextBatchTag = nextBatchTag;
  }
}
