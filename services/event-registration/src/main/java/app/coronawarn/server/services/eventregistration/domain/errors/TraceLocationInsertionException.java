package app.coronawarn.server.services.eventregistration.domain.errors;

public class TraceLocationInsertionException extends RuntimeException {

  public TraceLocationInsertionException(String message) {
    super(message);
  }
}
