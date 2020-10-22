package app.coronawarn.server.services.download.normalization;

/**
 * Thrown when a computed value for a data structure that is provided to mobile clients is not
 * alligned with the Exposure Notification framework (ENF) specifications.
 */
public class NotAnEnfValueException extends RuntimeException {

  private static final long serialVersionUID = 6945840073523978342L;

  public NotAnEnfValueException(int decodedDsos, String fieldName) {
    super(decodedDsos
        + " is not a " + fieldName + " value accepted by the Exposure Notification Framwork");
  }
}
