package app.coronawarn.server.services.submission.exception;

/**
 * Exception thrown to indicate an invalid parameter of a
 * {@link app.coronawarn.server.common.persistence.domain.DiagnosisKey}.
 */
public class InvalidPayloadException extends Exception {

  public InvalidPayloadException() {
  }

  public InvalidPayloadException(String message) {
    super(message);
  }

  public InvalidPayloadException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidPayloadException(Throwable cause) {
    super(cause);
  }

  public InvalidPayloadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
