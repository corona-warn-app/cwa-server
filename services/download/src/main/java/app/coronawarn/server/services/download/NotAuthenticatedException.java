package app.coronawarn.server.services.download;

public class NotAuthenticatedException extends RuntimeException {

  public NotAuthenticatedException(String message) {
    super(message);
  }
}
