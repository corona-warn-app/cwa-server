package app.coronawarn.server.services.distribution.dgc.exception;

public class FetchDscTrustListException extends Exception {

  public FetchDscTrustListException(String message) {
    super(message);
  }

  public FetchDscTrustListException(String message, Throwable cause) {
    super(message, cause);
  }
}
