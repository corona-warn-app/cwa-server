package app.coronawarn.server.services.distribution.assembly.appconfig.validation;

/**
 * The validation could not be executed. Find more information about the root cause in the cause element, and in the
 * message property.
 */
public class ValidationExecutionException extends RuntimeException {
  private static final long serialVersionUID = -3970814945560559417L;

  public ValidationExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
}
