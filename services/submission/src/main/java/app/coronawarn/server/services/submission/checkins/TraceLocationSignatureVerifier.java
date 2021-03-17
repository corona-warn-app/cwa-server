package app.coronawarn.server.services.submission.checkins;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import app.coronawarn.server.common.protocols.internal.evreg.SignedEvent;
import app.coronawarn.server.services.submission.config.CryptoProvider;

@Component
public class TraceLocationSignatureVerifier {

  private static final Logger logger = LoggerFactory.getLogger(EventCheckinDataFilter.class);

  private final CryptoProvider cryptoProvider;

  public TraceLocationSignatureVerifier(CryptoProvider cryptoProvider) {
    this.cryptoProvider = cryptoProvider;
  }

  public boolean verify(SignedEvent signedEvent) {
    byte[] signatureBytes = signedEvent.getSignature().toByteArray();
    byte[] contentBytes = signedEvent.getEvent().toByteArray();
    try {
      Signature signatureAlgorithm = Signature.getInstance(cryptoProvider.getSignatureAlgorithm());
      signatureAlgorithm.initVerify(cryptoProvider.getCertificate());
      signatureAlgorithm.update(contentBytes);
      return signatureAlgorithm.verify(signatureBytes);
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      logger
          .error("Can not initialize Checkin trace location signature verification due to invalid "
              + "public key or algorithm. Please check service configuration.");
      return false;
    } catch (SignatureException e) {
      logger.warn("Could not verify signature of checkin trace location");
      return false;
    }
  }
}
