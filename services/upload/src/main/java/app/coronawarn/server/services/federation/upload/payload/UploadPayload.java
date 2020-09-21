

package app.coronawarn.server.services.federation.upload.payload;

import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import java.util.List;

/**
 * This class represents an Upload call to the Federation Gateway. The payload for EFGS must contain the following
 * information:
 *
 * <p><ul>
 * <li> The bytes of the protobuf ({@link DiagnosisKeyBatch} batch). </li>
 * <li> The signature bytes (String batchSignature). </li>
 * <li> The unique batch tag (String batchTag). </li>
 * </p></ul>
 */
public class UploadPayload {

  private DiagnosisKeyBatch batch;
  private List<FederationUploadKey> originalKeys;
  private String batchSignature;
  private String batchTag;

  public DiagnosisKeyBatch getBatch() {
    return batch;
  }

  public UploadPayload setBatch(DiagnosisKeyBatch batch) {
    this.batch = batch;
    return this;
  }

  public String getBatchSignature() {
    return batchSignature;
  }

  public UploadPayload setBatchSignature(String batchSignature) {
    this.batchSignature = batchSignature;
    return this;
  }

  public String getBatchTag() {
    return batchTag;
  }

  public UploadPayload setBatchTag(String batchTag) {
    this.batchTag = batchTag;
    return this;
  }

  /**
   * Returns {@link DiagnosisKey} entities that are part of this upload payload.
   */
  public List<FederationUploadKey> getOriginalKeys() {
    return originalKeys;
  }

  public void setOriginalKeys(List<FederationUploadKey> originalKeys) {
    this.originalKeys = originalKeys;
  }
}
