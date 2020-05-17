package app.coronawarn.server.common.persistence.exception;

/**
 * Exception thrown to indicate an invalid parameter of a
 * {@link app.coronawarn.server.common.persistence.domain.DiagnosisKey},
 * meaning a field of a
 * {@link app.coronawarn.server.common.persistence.domain.DiagnosisKey}
 * is semantically erroneous.
 */
public class InvalidDiagnosisKeyException extends RuntimeException {

  public InvalidDiagnosisKeyException(String message) {
    super(message);
  }

}
