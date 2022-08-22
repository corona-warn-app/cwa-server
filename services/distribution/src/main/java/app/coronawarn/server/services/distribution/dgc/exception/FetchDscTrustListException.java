package app.coronawarn.server.services.distribution.dgc.exception;

public class FetchDscTrustListException extends Exception {
  private static final long serialVersionUID = 4730807672445241054L;

  public FetchDscTrustListException(String message) {
    super(message);
  }

  public FetchDscTrustListException(String message, Throwable cause) {
    super(message, cause);
  }
}
