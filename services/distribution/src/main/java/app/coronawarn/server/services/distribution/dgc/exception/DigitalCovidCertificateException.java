package app.coronawarn.server.services.distribution.dgc.exception;

public class DigitalCovidCertificateException extends Exception {

  public DigitalCovidCertificateException(String message) {
    super(message);
  }

  public DigitalCovidCertificateException(String message, Throwable cause) {
    super(message, cause);
  }

  public DigitalCovidCertificateException(Throwable cause) {
    super(cause);
  }
}
