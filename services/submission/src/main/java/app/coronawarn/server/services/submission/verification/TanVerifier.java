package app.coronawarn.server.services.submission.verification;

import org.springframework.stereotype.Component;

/**
 * The TanVerifier performs the verification of submission TANs.
 */
@Component("tanVerifier")
public class TanVerifier {

  /**
   * @param tan Submission TAN
   * @return {@literal true} if the specified TAN is valid, {@literal false} otherwise.
   */
  public boolean verifyTan(String tan) {
    return true;
  }
}
