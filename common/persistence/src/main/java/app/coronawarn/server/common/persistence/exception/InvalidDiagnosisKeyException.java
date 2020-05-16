package app.coronawarn.server.common.persistence.exception;

/**
 * Exception thrown to indicate an invalid parameter of a
 * {@link app.coronawarn.server.common.persistence.domain.DiagnosisKey}.
 */
public class InvalidDiagnosisKeyException extends Exception {

  public InvalidDiagnosisKeyException() {
  }

  public InvalidDiagnosisKeyException(String message) {
    super(message);
  }

  public InvalidDiagnosisKeyException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidDiagnosisKeyException(Throwable cause) {
    super(cause);
  }

  public InvalidDiagnosisKeyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
