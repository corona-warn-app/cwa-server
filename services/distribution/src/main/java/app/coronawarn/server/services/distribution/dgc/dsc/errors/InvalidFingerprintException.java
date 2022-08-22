package app.coronawarn.server.services.distribution.dgc.dsc.errors;

public class InvalidFingerprintException extends Exception {
  private static final long serialVersionUID = 5715798235468337392L;
  private static final String ERROR = "Obtaining service provider allow list failed";

  public InvalidFingerprintException() {
    super(ERROR);
  }

  public InvalidFingerprintException(Exception e) {
    super(ERROR, e);
  }
}
