package app.coronawarn.server.services.distribution.statistics.exceptions;

@SuppressWarnings("serial")
public class ConnectionException extends RuntimeException {
  public ConnectionException(final Throwable cause) {
    super("Failed to retrieve statistics file from Object Store", cause);
  }
}
