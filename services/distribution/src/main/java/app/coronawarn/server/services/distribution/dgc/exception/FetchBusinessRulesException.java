package app.coronawarn.server.services.distribution.dgc.exception;

public class FetchBusinessRulesException extends Exception {

  public FetchBusinessRulesException(String message) {
    super(message);
  }

  public FetchBusinessRulesException(String message, Throwable cause) {
    super(message, cause);
  }
}
