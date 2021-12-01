package app.coronawarn.server.services.distribution.dgc.dsc.errors;

public class InvalidFingerprintException extends Exception {

  public InvalidFingerprintException() {
    super("Obtaining service provider allow list failed");
  }
}
