package app.coronawarn.server.services.submission.verification.tan;

import app.coronawarn.server.services.submission.verification.AuthorizationType;

/**
 * Thrown when the application tried to validate the TAN key. The key value of the TAN depends on it's {@link
 * AuthorizationType}. Validation is executed on {@link AuthorizationType#isValidSyntax(String)}.
 */
public class IllegalTanAuthorizationKeyFormatException extends TanAuthorizationException {

  /**
   * Creates a new instance of the IllegalTanAuthorizationKeyFormatException for the specified key.
   *
   * @param key The key, which failed validation.
   */
  public IllegalTanAuthorizationKeyFormatException(String key) {
    super("Illegal format for key: " + key);
  }

}
