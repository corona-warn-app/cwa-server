package app.coronawarn.server.common.persistence.exception;

/**
 * Exception thrown to indicate an invalid parameter of a
 * {@link app.coronawarn.server.common.persistence.domain.DiagnosisKey}.
 */
public class InvalidDiagnosisKeyException extends Exception {

  public InvalidDiagnosisKeyException(String message) {
    super(message);
  }

}
