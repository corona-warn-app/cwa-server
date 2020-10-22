package app.coronawarn.server.services.distribution.logging;

public class LogMessages {

  public static final String APPLICATION_TERMINATED_ABNORMALLY = "Application terminated abnormally.";
  public static final String POSTGRESS_TSL_DISABLED_MESSAGE =
      "The distribution runner is started with postgres connection TLS disabled. "
          + "This should never be used in PRODUCTION!";
  static final String SHUTTING_DOWN_LOG4J2_MESSAGE = "Shutting down log4j2.";
  static final String NUMBER_OF_FAILED_OBJECT_STORE_OPERATIONS_EXCEEDED_THRESHOLD =
      "Number of failed object store operations exceeded threshold of {}.";
  static final String CLEARING_OUTPUT_DIRECTORY = "Clearing output directory...";
  static final String UPLOADING_MESSAGE = "... uploading {}";
  public static final String DELETING_ENTRIES_WITH_PREFIX = "Deleting {} entries with prefix {}";
  static final String QUERYING_DIAGNOSIS_KEYS_FROM_THE_DATABASE = "Querying diagnosis keys from the database...";
  static final String DISTRIBUTION_DATA_ASSEMBLED_SUCCESSFULLY = "Distribution data assembled successfully.";
  static final String DATA_PUSHED_TO_OBJECT_STORE_SUCCESSFULLY = "Data pushed to Object Store successfully.";
  static final String DISTRIBUTION_DATA_ASSEMBLY_FAILED = "Distribution data assembly failed.";
  static final String DISTRIBUTION_FAILED = "Distribution failed.";
  static final String RETENTION_POLICY_APPLIED_SUCCESSFULLY = "Retention policy applied successfully.";
  static final String APPLICATION_OF_RETENTION_POLICY_FAILED = "Application of retention policy failed.";
  static final String OBJECT_STORE_OPERATION_FAILED = "Object store operation failed.";
  public static final String PREPARING_FILES = "Preparing files...";
  public static final String START_SIGNING = "Start signing...";
  public static final String WRITING_FILES = "Writing files...";

  private LogMessages() {
  }
}
