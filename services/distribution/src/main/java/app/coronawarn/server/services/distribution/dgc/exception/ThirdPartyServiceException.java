package app.coronawarn.server.services.distribution.dgc.exception;

public class ThirdPartyServiceException extends Exception {

  private static final long serialVersionUID = -4516793074769890548L;

  public ThirdPartyServiceException(final String message) {
    super(message);
  }

  public ThirdPartyServiceException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
