package app.coronawarn.server.services.distribution.dgc.exception;

public class DigitalCovidCertificateSignatureException extends Exception {

  public DigitalCovidCertificateSignatureException(String message, Throwable cause) {
    super(message, cause);
  }

  public DigitalCovidCertificateSignatureException(String message) {
    super(message);
  }
}
