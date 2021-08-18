package app.coronawarn.server.services.submission.verification;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import java.util.List;

public class EventTanVerifier extends TanVerificationService {

  private static final Logger logger = LoggerFactory.getLogger(EventTanVerifier.class);

  /**
   * This class can be used to verify a PIW TAN (submission on behalf) against a configured verification service.
   *
   * @param verificationServerClient The REST client to communicate with the verification server
   */
  public EventTanVerifier(VerificationServerClient verificationServerClient) {
    super(verificationServerClient);
  }

  /**
   * Queries the configured verification service to validate the provided TAN.
   *
   * @param tan Submission Authorization TAN
   * @return {@literal true} if verification service is able to verify the provided TAN and if it is
   *      a 'submission on behalf' TAN, {@literal false} otherwise
   * @throws RestClientException if http status code is neither 2xx nor 404
   */
  boolean verifyWithVerificationService(Tan tan) {
    try {
      logger.info("Calling Verification Service for TAN verification ...");
      ResponseEntity<Void> result = verificationServerClient.verifyTan(tan);
      List<String> typeHeaders = result.getHeaders().get(CWA_TELETAN_TYPE_RESPONSE_HEADER);
      if (typeHeaders == null || !typeHeaders.contains(CWA_TELETAN_TYPE_EVENT)) {
        // TODO Which is the correct way to log a security incident?
        logger.warn("Given TAN should have been for submission-on-behalf (type EVENT), but was of type {}.", typeHeaders);
        return false;
      }
      logger.info("Received response from Verification Service: {}", result);
      return true;
    } catch (FeignException.NotFound e) {
      logger.info("Verification Service reported unverified TAN");
      return false;
    }
  }
}
