package app.coronawarn.server.services.submission.verification.tan;

import app.coronawarn.server.services.submission.verification.AuthorizationType;

/**
 * Thrown when the application tried to parse the CWA-Authorization header, but the header value did not comply with the
 * specification.
 * <br>
 * A valid header format is: CWA-Authorization: (AuthorizationType) (Key)
 * <ul>
 * <li>AuthorizationType: A value of {@link AuthorizationType}</li>
 * <li>Key: Characters/numbers and spaces, min 6 and max 30</li>
 * </ul>
 */
public class IllegalTanAuthorizationFormatException extends TanAuthorizationException {

  /**
   * Creates a new IllegalTanAuthorizationFormatException with the given TAN.
   *
   * @param tan the TAN, which failed the syntax check
   */
  public IllegalTanAuthorizationFormatException(String tan) {
    super("Not a valid format: " + tan);
  }

}
