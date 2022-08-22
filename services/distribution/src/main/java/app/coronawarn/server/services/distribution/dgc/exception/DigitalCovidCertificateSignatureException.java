package app.coronawarn.server.services.distribution.dgc.exception;

public class DigitalCovidCertificateSignatureException extends RuntimeException {
  private static final long serialVersionUID = -8753378064860398366L;

  public DigitalCovidCertificateSignatureException(String message, Throwable cause) {
    super(message, cause);
  }
}
