package app.coronawarn.server.services.submission.verification.tan;

/**
 * Thrown, when the application tried to validate the CWA-Authorization header value, in case the
 * given value is malformed.
 */
public class TanAuthorizationException extends Exception {

  public TanAuthorizationException(String message) {
    super(message);
  }
}
