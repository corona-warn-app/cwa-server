package app.coronawarn.server.services.distribution.parameters.validation;

/**
 * The validation could not be executed. Find more information about the root cause in the cause
 * element, and in the message property.<br>
 */
public class ValidationFailedException extends RuntimeException {

  public ValidationFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
