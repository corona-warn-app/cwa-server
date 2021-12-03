package app.coronawarn.server.services.distribution.dgc.dsc.errors;

public class InvalidContentResponseException extends Exception {
  private static final String ERROR = "Obtaining providers from content response failed";

  public InvalidContentResponseException() {
    super(ERROR);
  }
}
