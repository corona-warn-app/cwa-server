package app.coronawarn.server.services.distribution.dcc;

public class FetchDccListException extends Exception {

  public FetchDccListException(String message) {
    super(message);
  }

  public FetchDccListException(String message, Throwable cause) {
    super(message, cause);
  }
}
