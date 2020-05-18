package app.coronawarn.server.services.submission.exception;

/**
 * Exception thrown to indicate an invalid payload of a
 * {@link app.coronawarn.server.common.protocols.external.exposurenotification.Key}.
 */
public class InvalidPayloadException extends RuntimeException {

  public InvalidPayloadException(String message) {
    super(message);
  }

}
