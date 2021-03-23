package app.coronawarn.server.services.eventregistration.domain.errors;

public class SigningException extends RuntimeException {

  public SigningException(String message, Throwable cause) {
    super(message, cause);
  }
}
