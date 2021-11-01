package app.coronawarn.server.services.distribution.dgc.exception;

public class FetchBusinessRulesException extends ThirdPartyServiceException {

  private static final long serialVersionUID = 4012763865820913945L;

  public FetchBusinessRulesException(String message, Throwable cause) {
    super(message, cause);
  }

  public FetchBusinessRulesException(String message) {
    super(message);
  }
}
