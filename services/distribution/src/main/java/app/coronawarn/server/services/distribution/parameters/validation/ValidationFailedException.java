package app.coronawarn.server.services.distribution.parameters.validation;

public class ValidationFailedException extends RuntimeException {

  public ValidationFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
