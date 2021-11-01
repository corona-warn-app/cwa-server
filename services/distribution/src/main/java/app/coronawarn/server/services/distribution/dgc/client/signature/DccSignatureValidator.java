package app.coronawarn.server.services.distribution.dgc.client.signature;

import static app.coronawarn.server.common.shared.util.HashUtils.byteStringDigest;
import static app.coronawarn.server.common.shared.util.SecurityUtils.base64decode;
import static app.coronawarn.server.common.shared.util.SecurityUtils.ecdsaSignatureVerification;
import static app.coronawarn.server.common.shared.util.SecurityUtils.getPublicKeyFromString;

import app.coronawarn.server.common.shared.util.HashUtils.Algorithms;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateSignatureException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.function.BiFunction;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DccSignatureValidator {

  private static final Logger logger = LoggerFactory.getLogger(DccSignatureValidator.class);

  private final DistributionServiceConfig distributionServiceConfig;

  public DccSignatureValidator(DistributionServiceConfig distributionServiceConfig) {
    this.distributionServiceConfig = distributionServiceConfig;
  }

  /**
   * Verify ECDSA signature for DCC.
   *
   * @param signature - signature.
   * @param body - content to verify.
   * @throws IOException - thrown if error occurs.
   */
  public void checkSignature(String signature, String body) throws IOException {
    try {
      PublicKey publicKey = getPublicKeyFromString(
          distributionServiceConfig.getDigitalGreenCertificate().getClient().getPublicKey());

      byte[] stringyfiedContentSha = byteStringDigest(body, Algorithms.SHA_256);
      byte[] toHexString = Hex.toHexString(stringyfiedContentSha).getBytes(StandardCharsets.UTF_8);
      byte[] base64Signature = base64decode(signature);

      ecdsaSignatureVerification(base64Signature, publicKey, toHexString);
    } catch (NoSuchAlgorithmException e) {
      throw new DigitalCovidCertificateSignatureException("Specified algorithm is not available for Key Factory.", e);
    } catch (InvalidKeySpecException | InvalidKeyException e) {
      throw new DigitalCovidCertificateSignatureException("DCC Public key generation threw an error.", e);
    } catch (SignatureException e) {
      logger.warn("Invalid signature {} with public key {} for body: {}", signature,
          distributionServiceConfig.getDigitalGreenCertificate().getClient().getPublicKey(), body);
      throw signatureExceptionConverter().apply(e, "Invalid signature.");
    }
  }

  private BiFunction<GeneralSecurityException, String, IOException> signatureExceptionConverter() {
    return (exception, message) -> new IOException("DCC signature verification failed: " + message, exception);
  }
}
