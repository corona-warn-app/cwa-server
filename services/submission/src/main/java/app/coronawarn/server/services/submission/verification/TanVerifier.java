

package app.coronawarn.server.services.submission.verification;

import app.coronawarn.server.common.Logger;
import app.coronawarn.server.common.LoggerFactory;
import app.coronawarn.server.services.submission.logging.SubmissionLogMessages;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * The TanVerifier performs the verification of submission TANs.
 */
@Service
public class TanVerifier {

  private static final Logger logger = LoggerFactory.getLogger(TanVerifier.class);
  private final VerificationServerClient verificationServerClient;

  /**
   * This class can be used to verify a TAN against a configured verification service.
   *
   * @param verificationServerClient The REST client to communicate with the verification server
   */
  public TanVerifier(VerificationServerClient verificationServerClient) {
    this.verificationServerClient = verificationServerClient;
  }

  /**
   * Verifies the specified TAN. Returns {@literal true} if the specified TAN is valid, {@literal false} otherwise.
   *
   * @param tanString Submission Authorization TAN
   * @return {@literal true} if the specified TAN is valid, {@literal false} otherwise.
   * @throws RestClientException if status code is neither 2xx nor 4xx
   */
  public boolean verifyTan(String tanString) {
    try {
      Tan tan = Tan.of(tanString);

      return verifyWithVerificationService(tan);
    } catch (IllegalArgumentException e) {
      logger.error(SubmissionLogMessages.TAN_VERIFICATION_FAILED_MESSAGE,
          tanString.substring(0, Math.min(36, tanString.length())), tanString.length());
      return false;
    }
  }

  /**
   * Queries the configured verification service to validate the provided TAN.
   *
   * @param tan Submission Authorization TAN
   * @return {@literal true} if verification service is able to verify the provided TAN, {@literal false} otherwise
   * @throws RestClientException if http status code is neither 2xx nor 404
   */
  private boolean verifyWithVerificationService(Tan tan) {
    try {
      logger.info(SubmissionLogMessages.TAN_VERIFICATION_SERVICE_CALLED_MESSAGE);
      verificationServerClient.verifyTan(tan);
      logger.info(SubmissionLogMessages.TAN_VERIFICATION_RESPONSE_RECEIVED);
      return true;
    } catch (FeignException.NotFound e) {
      logger.info(SubmissionLogMessages.UNVERIFIED_TAN_MESSAGE);
      return false;
    }
  }
}
