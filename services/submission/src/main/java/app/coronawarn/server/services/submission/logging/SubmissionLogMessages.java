package app.coronawarn.server.services.submission.logging;

import app.coronawarn.server.common.LogMessages;

public enum SubmissionLogMessages implements LogMessages {
  //Exception Handling ---------------------------------
  UNKNOWN_EXCEPTION_MESSAGE("Unable to handle {}"),
  BINDING_EXCEPTION_MESSAGE("Binding failed {}"),
  DIAGNOSIS_KEY_EXCEPTION_MESSAGE("Erroneous Submission Payload {}"),

  //Submission Controller Handling ------------------------------
  RETENTION_THRESHOLD_EXCEEDED_MESSAGE(
      "Not persisting {} diagnosis key(s), as it is outdated beyond retention threshold."),
  SUBMISSION_MISSING_KEY_LEVEL6_RISK_MESSAGE(
      "Submission payload was sent with missing key having transmission risk level 6. {}"),
  SUBMISSION_KEY_LEVEL6_RISK_MESSAGE(
      "Submission payload was sent with key having transmission risk level 6. {}"),
  SUBMISSION_KEY_LEVEL6_RISK_START_INTERVAL_TODAY_MESSAGE(
      "Submission payload was sent with a key having transmission risk level 6"
          + " and rolling start interval number of today midnight. {}"),

  // TAN Verification ---------------------------------
  TAN_VERIFICATION_FAILED_MESSAGE("TAN Syntax check failed for TAN: {}, length: {}"),
  TAN_VERIFICATION_RESPONSE_RECEIVED("Received response from Verification Service"),
  TAN_VERIFICATION_SERVICE_CALLED_MESSAGE("Calling Verification Service for TAN verification ..."),
  UNVERIFIED_TAN_MESSAGE("Verification Service reported unverified TAN");

  private String message;

  SubmissionLogMessages(String message) {
    this.message = message;
  }

  /**
   * Returns the message that should be logged.
   *
   * @return the log message (default English).
   */
  @Override
  public String toString() {
    return this.message;
  }
}
