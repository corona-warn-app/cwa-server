package app.coronawarn.server.services.federation.upload.payload.signing;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import org.springframework.stereotype.Component;

@Component
public class BatchSigner {

  private final CryptoProvider cryptoProvider;

  private final UploadServiceConfig uploadServiceConfig;

  public BatchSigner(CryptoProvider cryptoProvider,
      UploadServiceConfig uploadServiceConfig) {
    this.cryptoProvider = cryptoProvider;
    this.uploadServiceConfig = uploadServiceConfig;
  }

  /**
   * Generate the signature bytes based on {@link DiagnosisKeyBatch}.
   * @param batch {@link DiagnosisKeyBatch}.
   * @return signature bytes.
   * @throws GeneralSecurityException if private key was not properly loaded.
   */
  public byte[] createSignatureBytes(DiagnosisKeyBatch batch) throws GeneralSecurityException {
    PrivateKey privateKey = this.cryptoProvider.getPrivateKey();
    if (privateKey == null || privateKey.isDestroyed()) {
      throw new GeneralSecurityException("Private Key not loaded");
    }
    Signature payloadSignature = Signature.getInstance(uploadServiceConfig.getSignature().getAlgorithmName(),
        uploadServiceConfig.getSignature().getSecurityProvider());
    payloadSignature.initSign(privateKey);
    payloadSignature.update(batch.toByteArray());
    return payloadSignature.sign();
  }

}
