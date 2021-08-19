

package app.coronawarn.server.services.submission.verification;

import feign.FeignException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * The TanVerifier performs the verification of submission TANs.
 */
@Service
public class TanVerifier extends TanVerificationService {

  private static final Logger logger = LoggerFactory.getLogger(TanVerifier.class);

  /**
   * This class can be used to verify a TAN against a configured verification service.
   *
   * @param verificationServerClient The REST client to communicate with the verification server
   */
  public TanVerifier(VerificationServerClient verificationServerClient) {
    super(verificationServerClient);
  }

  /**
   * Queries the configured verification service to validate the provided TAN.
   *
   * @param tan Submission Authorization TAN
   * @return {@literal true} if verification service is able to verify the provided TAN and the TAN is no 'submission on
   *     behalf' TAN, {@literal false} otherwise
   * @throws RestClientException if http status code is neither 2xx nor 404
   */
  boolean verifyWithVerificationService(Tan tan) {
    try {
      logger.info("Calling Verification Service for TAN verification ...");
      ResponseEntity<Void> result = verificationServerClient.verifyTan(tan);
      List<String> typeHeaders = result.getHeaders().getOrEmpty(CWA_TELETAN_TYPE_RESPONSE_HEADER);

      final Optional<String> teleTanHeader = typeHeaders.stream().findFirst();
      if (teleTanHeader.isPresent() && teleTanHeader.get().equalsIgnoreCase(CWA_TELETAN_TYPE_EVENT)) {
        logger.warn(SECURITY, "Given TAN should have been for regular submission, but was of type {} .",
            teleTanHeader.get());
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
