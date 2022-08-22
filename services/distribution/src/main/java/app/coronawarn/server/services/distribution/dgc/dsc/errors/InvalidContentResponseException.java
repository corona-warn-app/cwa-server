package app.coronawarn.server.services.distribution.dgc.dsc.errors;

public class InvalidContentResponseException extends Exception {
  private static final long serialVersionUID = -4419103572626924400L;
  private static final String ERROR = "Obtaining providers from content response failed";

  public InvalidContentResponseException() {
    super(ERROR);
  }
}
