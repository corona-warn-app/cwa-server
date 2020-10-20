package app.coronawarn.server.common.persistence.service.common;

import app.coronawarn.server.common.LogMessages;

public enum PersistenceLogMessages implements LogMessages {

  DELETING_BATCH_INFOS_FOR_DATE("Deleting {} batch info(s) for date {}."), //
  DELETING_BATCH_INFOS_WITH_DATE_OLDER("Deleting {} batch info(s) with a date older than {} day(s) ago."), //
  DELETING_DIAGNOSIS_KEYS_WITH_SUBMISSION_TIMESTAMP_OLDER(
      "Deleting {} diagnosis key(s) with a submission timestamp older than {} day(s) ago."), //
  DIAGNOSIS_KEYS_CONFLICTED_WITH_DB_ENTRIES(
      "{} out of {} diagnosis keys conflicted with existing database entries and were ignored."), //
  KEYS_PICKED_FROM_UPLOAD_TABLE("{} keys picked after read from upload table"), //
  KEYS_REMAINING_AFTER_FILTERING_BY_CONSENT("{} keys remaining after filtering by consent"), //
  KEYS_REMAINING_AFTER_FILTERING_BY_SHARE_POLICY("{} keys remaining after filtering by share policy"), //
  KEYS_REMAINING_AFTER_FILTERING_BY_VALIDITY("{} keys remaining after filtering by validity"), //
  KEYS_SELECTED_FOR_UPLOAD("Keys selected for upload: {}"), //
  MARKET_BATCH_WITH_STATUS("Marked batch {} with status {}."), //
  NR_RETRIEVED_DISCARDED_DIAGNOSIS_KEYS(
      "Retrieved {} diagnosis key(s). Discarded {} diagnosis key(s) from the result as invalid."), //
  VALIDATION_FAILED_WITH_VIOLATIONS("Validation failed for diagnosis key from database. Violations: {}"), //
  ;

  private final String message;

  /**
   * Returns the message that should be logged.
   *
   * @return the log message (default English).
   */
  @Override
  public String toString() {
    return message;
  }

  private PersistenceLogMessages(String message) {
    this.message = message;
  }

}
