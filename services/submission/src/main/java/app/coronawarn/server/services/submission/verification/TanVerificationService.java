package app.coronawarn.server.services.submission.verification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.web.client.RestClientException;

public abstract class TanVerificationService {

  public static final String CWA_TELETAN_TYPE_RESPONSE_HEADER = "X-CWA-TELETAN-TYPE";
  public static final String CWA_TELETAN_TYPE_EVENT = "EVENT";
  protected static final Marker SECURITY = MarkerFactory.getMarker("SECURITY");


  private static final Logger logger = LoggerFactory.getLogger(TanVerifier.class);
  final VerificationServerClient verificationServerClient;

  /**
   * This class can be used to verify a TAN against a configured verification service.
   *
   * @param verificationServerClient The REST client to communicate with the verification server
   */
  public TanVerificationService(VerificationServerClient verificationServerClient) {
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
      logger.error("TAN Syntax check failed for TAN: {}, length: {}",
          tanString.substring(0, Math.min(36, tanString.length())), tanString.length());
      return false;
    }
  }

  /**
   * Implement to differentiate between validating a regular submission TAN and a submission-on-behalf TAN
   * (#CWA_TELETAN_TYPE_RESPONSE_HEADER has value #CWA_TELETAN_TYPE_RESPONSE_HEADER).
   *
   * @param tan the tan that will be verified either for normal submission or submission on behalf.
   * @return whether the implemented logic returns that the tan is valid or not.
   */
  abstract boolean verifyWithVerificationService(Tan tan);

}
