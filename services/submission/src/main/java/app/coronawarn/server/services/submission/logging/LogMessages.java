package app.coronawarn.server.services.submission.logging;

public enum LogMessages {
  //Exception Handling ---------------------------------
  UNKNOWN_EXCEPTION_MESSAGE("Unable to handle {}"),
  BINDING_EXCEPTION_MESSAGE("Binding failed {}"),
  DIAGNOSIS_KEY_EXCEPTION_MESSAGE("Erroneous Submission Payload {}"),

  //Submission Controller Handling ------------------------------
  RETENTION_TRESHOLD_EXCEEDED_MESSAGE(
      "Not persisting {} diagnosis key(s), as it is outdated beyond retention threshold."),
  SUBMISSION_MISSING_KEY_LEVEL6_RISK_MESSAGE(
      "Submission payload was sent with missing key having transmission risk level 6. {}"),
  SUBMISSION_KEY_LEVEL6_RISK_MESSAGE(
      "Submission payload was sent with key having transmission risk level 6. {}"),
  SUBMISSION_KEY_LEVEL6_RISK_START_INTERVAL_TODAY_MESSAGE(
      "Submission payload was sent with a key having transmission risk level 6"
          + " and rolling start interval number of today midnight. {}");

  private String message;

  LogMessages(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return this.message;
  }
}
