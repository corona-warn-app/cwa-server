package app.coronawarn.server.services.eventregistration.domain.errors;

@SuppressWarnings("serial")
public class TraceLocationInsertionException extends RuntimeException {

  public TraceLocationInsertionException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
