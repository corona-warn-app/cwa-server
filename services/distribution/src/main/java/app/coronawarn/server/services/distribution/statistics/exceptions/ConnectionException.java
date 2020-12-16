package app.coronawarn.server.services.distribution.statistics.exceptions;

public class ConnectionException extends RuntimeException {
  public ConnectionException() {
    super("Failed to retrieve statistics file from Object Store");
  }
}
