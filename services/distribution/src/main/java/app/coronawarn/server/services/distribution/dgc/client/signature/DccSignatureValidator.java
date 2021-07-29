package app.coronawarn.server.services.distribution.dgc.client.signature;

import static app.coronawarn.server.common.shared.util.HashUtils.byteStringDigest;
import static app.coronawarn.server.common.shared.util.SecurityUtils.ecdsaSignatureVerification;
import static app.coronawarn.server.common.shared.util.SecurityUtils.getEcdsaEncodeFromSignature;
import static app.coronawarn.server.common.shared.util.SecurityUtils.getPublicKeyFromString;
import static app.coronawarn.server.common.shared.util.SerializationUtils.stringifyObject;

import app.coronawarn.server.common.shared.util.HashUtils.Algorithms;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateSignatureException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class DccSignatureValidator {

  public static final String X_SIGNATURE = "X-SIGNATURE";

  /**
   * Check signature for DCC response.
   * @param responseEntity - DCC response.
   * @throws DigitalCovidCertificateSignatureException - thrown if signature verification fails or any issue
   *     occurs on the way to verify it.
   */
  public void checkSignature(ResponseEntity<?> responseEntity) throws DigitalCovidCertificateSignatureException {
    List<String> header = responseEntity.getHeaders().get(X_SIGNATURE);
    if (header == null || header.isEmpty()) {
      throw new DigitalCovidCertificateSignatureException("X-SIGNATURE header is not present");
    } else {
      verifyEcdsaSignature(header.get(0), responseEntity.getBody());
    }
  }

  /**
   * Verify ECDSA signature for DCC.
   * @param signature - signature.
   * @param content - content to verify.
   * @throws DigitalCovidCertificateSignatureException - thrown if error occurs.
   */
  private void verifyEcdsaSignature(String signature, Object content) throws DigitalCovidCertificateSignatureException {
    try {
      PublicKey publicKey = getPublicKeyFromString("publicKey");
      String stringyfiedContent = stringifyObject(content);
      byte[] ecdsaSignature = getEcdsaEncodeFromSignature(byteStringDigest(signature, Algorithms.SHA_256));
      ecdsaSignatureVerification(ecdsaSignature, publicKey, stringyfiedContent);
    } catch (NoSuchAlgorithmException | IOException e) {
      throw new DigitalCovidCertificateSignatureException("A problem occured before verifying the DCC signature: ", e);
    } catch (InvalidKeySpecException | InvalidKeyException e) {
      throw new DigitalCovidCertificateSignatureException("DCC Public key generation throwed an error: ", e);
    } catch (SignatureException e) {
      throw new DigitalCovidCertificateSignatureException("DCC Signature verification failed: ", e);
    }
  }
}
