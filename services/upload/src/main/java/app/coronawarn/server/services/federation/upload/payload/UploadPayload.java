package app.coronawarn.server.services.federation.upload.payload;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;

public class UploadPayload {

  private DiagnosisKeyBatch batch;
  private String batchSignature;
  private String batchTag;

  public DiagnosisKeyBatch getBatch() {
    return batch;
  }

  public void setBatch(DiagnosisKeyBatch batch) {
    this.batch = batch;
  }

  public String getBatchSignature() {
    return batchSignature;
  }

  public void setBatchSignature(String batchSignature) {
    this.batchSignature = batchSignature;
  }

  public String getBatchTag() {
    return batchTag;
  }

  public void setBatchTag(String batchTag) {
    this.batchTag = batchTag;
  }
}
