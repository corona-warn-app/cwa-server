package app.coronawarn.server.services.submission.verification;

import app.coronawarn.server.services.submission.verification.tan.TanAuthorization;
import app.coronawarn.server.services.submission.verification.tan.TanAuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The TanVerifier performs the verification of submission TANs.
 */
@Component("tanVerifier")
public class TanVerifier {

  private static final Logger logger = LoggerFactory.getLogger(TanVerifier.class);

  /**
   * Verifies the specified TAN. Returns {@literal true} if the specified TAN is valid, {@literal
   * false} otherwise.
   *
   * @param tan Submission Authorization TAN, in format "(TAN|TELETAN) KEY"
   * @return {@literal true} if the specified TAN is valid, {@literal false} otherwise.
   */
  public boolean verifyTan(String tan) {
    try {
      var tanAuthorization = TanAuthorization.of(tan);

      return validateAuthorization(tanAuthorization);
    } catch (TanAuthorizationException e) {
      logger.info("Failed TAN validation", e);
      return false;
    }
  }

  private boolean validateAuthorization(TanAuthorization tanAuthorization) {
    var authType = tanAuthorization.getAuthType();

    switch (authType) {
      case TAN:
        return validateTan(tanAuthorization.getKey());
      case TELETAN:
        return validateTeleTan(tanAuthorization.getKey());
      default:
        throw new UnsupportedOperationException("Authorization Type not supported: " + authType);
    }
  }

  private boolean validateTan(String tan) {
    return true;
  }

  private boolean validateTeleTan(String teleTan) {
    return true;
  }
}
