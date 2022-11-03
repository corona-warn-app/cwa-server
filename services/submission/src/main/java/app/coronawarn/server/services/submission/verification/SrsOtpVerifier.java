package app.coronawarn.server.services.submission.verification;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The SRS OTP verifier performs the verification of SRS OTPs.
 */
@Service
public class SrsOtpVerifier extends TanVerificationService {

  private static final Logger logger = LoggerFactory.getLogger(SrsOtpVerifier.class);

  public SrsOtpVerifier(VerificationServerClient verificationServerClient) {
    super(verificationServerClient);
  }

  boolean verifyWithVerificationService(Tan tan) {
    try {
      logger.info("Calling SRS OPT verification Service ...");

      // FIXME!
      // ResponseEntity<Void> result = verificationServerClient.verifyTan(tan);
      // List<String> typeHeaders = result.getHeaders().getOrEmpty(CWA_TELETAN_TYPE_RESPONSE_HEADER);
      // logger.info("Received response from Verification Service: {}", result);

      return true;
    } catch (FeignException.NotFound e) {
      logger.info("SRS OTP verification service reported: NotFound", e);
      return false;
    }
  }
}
