package app.coronawarn.server.services.federation.upload.payload;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;

/**
 * This class represents an Upload call to the Federation Gateway. The payload for EFGS must contain the following
 * information:
 * - The bytes of the protobuf ({@link DiagnosisKeyBatch} batch).
 * - The signature bytes (String batchSignature).
 * - The unique batch tag (String batchTag).
 */
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
