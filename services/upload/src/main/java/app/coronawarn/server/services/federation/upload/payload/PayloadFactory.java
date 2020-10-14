

package app.coronawarn.server.services.federation.upload.payload;

import static app.coronawarn.server.services.federation.upload.UploadLogMessages.FAILED_TO_GENERATE_UPLOAD_PAYLOAD_SIGNATURE;

import app.coronawarn.server.common.Logger;
import app.coronawarn.server.common.LoggerFactory;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.federation.upload.payload.signing.BatchSigner;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.stereotype.Component;

@Component
public class PayloadFactory {

  private static final Logger logger = LoggerFactory.getLogger(PayloadFactory.class);

  private final DiagnosisKeyBatchAssembler assembler;
  private final BatchSigner signer;

  public PayloadFactory(DiagnosisKeyBatchAssembler assembler, BatchSigner signer) {
    this.assembler = assembler;
    this.signer = signer;
  }

  private UploadPayload mapToPayloadAndSign(String batchTag, DiagnosisKeyBatch batch,
      List<FederationUploadKey> originalKeys) {
    var payload = new UploadPayload();
    payload.setBatch(batch);
    payload.setBatchTag(batchTag);
    payload.setOriginalKeys(originalKeys);
    try {
      payload.setBatchSignature(signer.createSignatureBytes(batch));
    } catch (GeneralSecurityException | OperatorCreationException | IOException | CMSException e) {
      logger.error(FAILED_TO_GENERATE_UPLOAD_PAYLOAD_SIGNATURE, e);
    }
    return payload;
  }

  private String generateBatchTag(int counter, byte[] runnerHash) {
    var currentTime = LocalDateTime.now(ZoneOffset.UTC);
    return String.format("%d-%d-%d-%s-%d",
        currentTime.getYear(),
        currentTime.getMonth().getValue(),
        currentTime.getDayOfMonth(),
        Base64.encodeBase64String(runnerHash),
        counter);
  }

  /**
   * Generates the Payload objects based on a list of Diagnosis Keys. This method will generate batches, add a proper
   * batch tag and sign them with the server private key.
   *
   * @param diagnosisKeys List of Diagnosis Keys.
   * @return upload payload object {@link UploadPayload}.
   */
  public List<UploadPayload> makePayloadList(List<FederationUploadKey> diagnosisKeys) {
    Map<DiagnosisKeyBatch, List<FederationUploadKey>> batchesAndOriginalKeys = assembler
        .assembleDiagnosisKeyBatch(diagnosisKeys);
    byte[] hash = new byte[4];
    new SecureRandom().nextBytes(hash);
    AtomicInteger batchCounter = new AtomicInteger(0);

    return batchesAndOriginalKeys.entrySet().stream()
        .map(entry -> this.mapToPayloadAndSign(
            generateBatchTag(batchCounter.getAndIncrement(), hash),
            entry.getKey(),
            entry.getValue()))
        .collect(Collectors.toList());
  }
}
