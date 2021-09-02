package app.coronawarn.server.services.distribution.dgc.exception;

public class DigitalCovidCertificateException extends ThirdPartyServiceException {

  private static final long serialVersionUID = -8598954326765631471L;

  public DigitalCovidCertificateException(String message) {
    super(message);
  }

  public DigitalCovidCertificateException(String message, Throwable cause) {
    super(message, cause);
  }
}
