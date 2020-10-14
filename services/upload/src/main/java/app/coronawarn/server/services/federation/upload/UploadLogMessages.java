package app.coronawarn.server.services.federation.upload;

import app.coronawarn.server.common.LogMessages;

public enum UploadLogMessages implements LogMessages {

  RESPONSE_STATUS_FROM_EFGS("Response Status from EFGS: {}"), //
  FAKE_BATCH_UPLOAD("Calling fake batch upload with: \n\tkeys:{}\n\tbatchTag:{}\n\tbatchSignature:{}"), //
  FAILED_BYTE_ARRAY_TO_STRING_CONVERSION("Failed to convert byte array to string"), //
  BATCHES_NOT_GENERATED_NO_PENDING_UPLOAD_DIAGNOSIS_KEYS(
      "Batches not generated: no pending upload diagnosis keys found."), //
  BATCHES_NOT_GENERATED_NOT_MINIMUM_PENDING_UPLOAD_DIAGNOSIS_KEYS(
      "Batches not generated: less then minimum {} pending upload diagnosis keys."), //
  FAILED_TO_GENERATE_UPLOAD_PAYLOAD_SIGNATURE("Failed to generate upload payload signature"), //
  FOUND_PENDING_UPLOAD_KEYS("Found {} pending upload keys on DB"), //
  GENERATING_FAKE_UPLOAD_KEYS("Generating {} fake upload keys between times {} and {}"), //
  SKIPPING_GENERATION("Skipping generation"), //
  STORING_KEYS_IN_DB("Storing keys in the DB"), //
  FINISHED_TEST_DATA_GENERATION("Finished Test Data Generation Step"), //
  UPLOAD_SERVICE_TERMINATED_ABNORMALLY("Federation Upload Service terminated abnormally."), //
  SHUTTING_DOWN_LOG4J2("Shutting down log4j2."), //
  ENABLED_NAMED_GROUPS("Enabled named groups: {}"), //
  UPLOAD_SERVICE_STARTED_WITH_POSTGRES_TLS_DISABLED(
      "The upload service is started with postgres connection TLS disabled. "
          + "This should never be used in PRODUCTION!"), //
  EXECUTING_BATCH_REQUESTS("Executing batch request(s): {}"), //
  KEYS_NOT_PROCESSED_CORRECTLY("Some keys were not processed correctly"), //
  NR_KEYS_SUCCESSFUL("{} keys marked with status 201 (Successful)"), //
  NR_KEYS_CONFLICT("{} keys marked with status 409 (Conflict)"), //
  NR_KEYS_RETRY("{} keys marked with status 500 (Retry)"), //
  ALL_KEYS_PROCESSED_SUCCESSFULLY("All keys processed successfully"), //
  RUNNING_UPLOAD_JOB("Running Upload Job"), //
  GENERATING_UPLOAD_PAYLOAD("Generating Upload Payload for {} keys"), //
  EXECUTING_BATCH_REQUEST("Executing {} batch request"), //
  UPLOAD_DIAGNOSIS_KEY_DATA_FAILED("Upload diagnosis key data failed."), //
  MARKING_OF_DIAGNOSIS_KEYS_WITH_BATCH_TAG_ID_FAILED(
      "Post-upload marking of diagnosis keys with batch tag id failed"), //
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

  private UploadLogMessages(String message) {
    this.message = message;
  }

}
