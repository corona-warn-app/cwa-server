package app.coronawarn.server.common.persistence.exception;

/**
 * Exception thrown to indicate an invalid parameter of a
 * {@link app.coronawarn.server.common.persistence.domain.DiagnosisKey},
 * meaning a field of a
 * {@link app.coronawarn.server.common.persistence.domain.DiagnosisKey}
 * is semantically erroneous.
 */
public class InvalidDiagnosisKeyException extends RuntimeException {
  private static final long serialVersionUID = -6124348897420468935L;

  public InvalidDiagnosisKeyException(String message) {
    super(message);
  }

}
