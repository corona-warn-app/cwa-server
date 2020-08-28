package app.coronawarn.server.services.federation.upload.payload;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.federation.upload.payload.signing.BatchSigner;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PayloadFactory {

  private static final Logger logger = LoggerFactory
      .getLogger(PayloadFactory.class);

  private final DiagnosisKeyBatchAssembler assembler;
  private final BatchSigner signer;
  private final Random random = new Random();

  public PayloadFactory(DiagnosisKeyBatchAssembler assembler, BatchSigner signer) {
    this.assembler = assembler;
    this.signer = signer;
  }

  private UploadPayload mapToPayloadAndSign(DiagnosisKeyBatch batch) {
    var payload = new UploadPayload();
    payload.setBatch(batch);
    try {
      payload.setBatchSignature(new String(Base64.getEncoder().encode(signer.createSignatureBytes(batch))));
    } catch (GeneralSecurityException e) {
      logger.error("Failed to generate upload payload signature", e);
    }
    return payload;
  }

  private String generateBatchTag() {
    return Integer.toString(this.random.nextInt());
  }

  /**
   * Generates the Payload objects based on a list of Diagnosis Keys. This method will generate batches, add a proper
   * batch tag and sign them with the server private key.
   *
   * @param diagnosisKeys List of Diagnosis Keys.
   * @return upload payload object {@link UploadPayload}.
   */
  public List<UploadPayload> makePayloadList(List<DiagnosisKey> diagnosisKeys) {
    var batches = assembler.assembleDiagnosisKeyBatch(diagnosisKeys);
    var batchTag = this.generateBatchTag();
    return batches.stream()
        .map(this::mapToPayloadAndSign)
        .map(p -> p.setBatchTag(batchTag))
        .collect(Collectors.toList());
  }

}
