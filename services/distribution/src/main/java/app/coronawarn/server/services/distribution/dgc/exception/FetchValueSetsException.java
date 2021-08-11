package app.coronawarn.server.services.distribution.dgc.exception;

public class FetchValueSetsException extends ThirdPartyServiceException {

  private static final long serialVersionUID = -5697639773840761363L;

  public FetchValueSetsException(String message, Throwable cause) {
    super(message, cause);
  }

  public FetchValueSetsException(String message) {
    super(message);
  }
}
