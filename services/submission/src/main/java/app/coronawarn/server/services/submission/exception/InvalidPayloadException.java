package app.coronawarn.server.services.submission.exception;

/**
 * Exception thrown to indicate an invalid parameter of a
 * {@link app.coronawarn.server.common.persistence.domain.DiagnosisKey}.
 */
public class InvalidPayloadException extends Exception {

  public InvalidPayloadException(String message) {
    super(message);
  }

}
