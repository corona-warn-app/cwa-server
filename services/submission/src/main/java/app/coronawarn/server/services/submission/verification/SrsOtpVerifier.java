package app.coronawarn.server.services.submission.verification;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * The SRS OTP verifier performs the verification of SRS OTPs.
 */
@Service
public class SrsOtpVerifier extends TanVerificationService {

  private static final Logger logger = LoggerFactory.getLogger(SrsOtpVerifier.class);

  private final SrsVerifyClient client;

  public SrsOtpVerifier(final SrsVerifyClient client) {
    super(null);
    this.client = client;
  }

  @Override
  boolean verifyWithVerificationService(final Tan tan) {
    try {
      logger.debug("Calling SRS OPT verification Service ...");

      final ResponseEntity<SrsOtpRedemptionResponse> result = client.verifyOtp(Otp.of(tan));
      logger.info("Received response from SRS-verify: {}", result);
      if (result.getStatusCode().is2xxSuccessful() && OtpState.VALID.equals(result.getBody().getState())) {
        return true;
      }
      logger.warn("SRS-verify reponse: '{}'", result.getBody());
    } catch (final FeignException.NotFound e) {
      logger.warn("SRS OTP verification service reported: NotFound", e);
    }
    return false;
  }
}
