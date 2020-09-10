package app.coronawarn.server.services.federation.upload.payload;

import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.federation.upload.payload.signing.BatchSigner;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PayloadFactory {

  private static final Logger logger = LoggerFactory
      .getLogger(PayloadFactory.class);

  private final DiagnosisKeyBatchAssembler assembler;
  private final BatchSigner signer;

  public PayloadFactory(DiagnosisKeyBatchAssembler assembler, BatchSigner signer) {
    this.assembler = assembler;
    this.signer = signer;
  }

  private UploadPayload mapToPayloadAndSign(Integer batchCounter, DiagnosisKeyBatch batch,
      List<FederationUploadKey> originalKeys) {
    var payload = new UploadPayload();
    payload.setBatch(batch);
    payload.setBatchTag(this.generateBatchTag(batchCounter));
    payload.setOriginalKeys(originalKeys);
    try {
      payload.setBatchSignature(signer.createSignatureBytes(batch));
    } catch (GeneralSecurityException e) {
      logger.error("Failed to generate upload payload signature", e);
    } catch (OperatorCreationException | IOException | CMSException e) {
      e.printStackTrace();
    }
    return payload;
  }

  private String generateBatchTag(int counter) {
    var currentTime = LocalDateTime.now(ZoneOffset.UTC);
    return String.format("%d-%d-%d_%dh-%d",
        currentTime.getYear(),
        currentTime.getMonth().getValue(),
        currentTime.getDayOfMonth(),
        currentTime.getHour(),
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
    AtomicInteger batchCounter = new AtomicInteger(0);

    return batchesAndOriginalKeys.entrySet().stream()
        .map(entry -> this.mapToPayloadAndSign(batchCounter.incrementAndGet(),entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }

}
