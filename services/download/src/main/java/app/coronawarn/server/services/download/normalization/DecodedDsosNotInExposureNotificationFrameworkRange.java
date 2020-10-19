package app.coronawarn.server.services.download.normalization;

public class DecodedDsosNotInExposureNotificationFrameworkRange extends RuntimeException {

  private static final long serialVersionUID = 6945840073523978342L;

  public DecodedDsosNotInExposureNotificationFrameworkRange(int decodedDsos) {
    super(decodedDsos
        + " is not a 'daysSinceOnsetOfSynptoms' value accepted by the Exposure Notification Framwork");
  }
}
