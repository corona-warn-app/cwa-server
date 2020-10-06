# CWA-Server Download Service

## Spring Profiles

Spring profiles are used to apply submission service configuration based on the running environment, determined by the active profile.

You will find `.yaml` and `.xml` based profile-specific configuration files at [`/services/download/src/main/resources`](/services/download/src/main/resources).

### Available Profiles

Profile                                           | Effect
--------------------------------------------------|-------------
`dev`                                             | Sets the log level to `DEBUG` and changes the `CONSOLE_LOG_PATTERN` used by Log4j 2.
`cloud`                                           | Removes default values for the `spring.flyway`, `spring.datasource` and `services.submission.verification.base-url` configurations.
`disable-ssl-client-postgres`                     | Disables SSL with a pinned certificate for the connection to the postgres.

Please refer to the inline comments in the base `application.yaml` configuration file for further details on the configuration properties impacted by the above profiles.

## Environmental Veriables

Download specific environmentals:
Variable Name                    | Default Value  | Description
---------------------------------|----------------|-------------
EFGS_OFFSET_DAYS                 | 1              | The offset in days for which the keys shall be downloaded (must be in range 0 - 14).
ALLOWED_REPORT_TYPES_TO_DOWNLOAD | CONFIRMED_TEST | Accepted ReportTypes for download.

## Download Runner

The Download Runner triggers the download of Diagnosis Keys (DK) from the EFGS. It then triggers the processing of unprocessed batches and of those which caused errors in previous processing attempts.

## Batch Processing

The FederationBatchProcessor processes batches in sequence and persists all DKs that pass validation.

## Diagnosis Key Validation

Validation constraints enforce that each Key is compliant to the specifications.

### Enumerative Validation Constraints

Enumerative validation constraints are stored as environmental variables and can be consulted in the Download Services `application.yaml` (under `services`.`download`.`validation`).

Name                 | Default Value  | Description
---------------------|----------------|-------------
key-length           | 16             | Exact length of accepted DK Data.
allowed-report-types | CONFIRMED_TEST | Accepted ReportTypes.
max-dsos             | 4000           | Accepted upper bound for Days Since Onset of Symptoms.
min-rolling-period   | 0              | Accepted lower bound for Rolling Period.
max-rolling-period   | 144            | Accepted upper bound for Rolling Period.
min-trl              | 0              | Accepted lower bound for Transmission Risk Level.
max-trl              | 8              | Accepted upper bound for Transmission Risk Level.

### Validation Checks

The checks performed on downloaded DKs are as follows:

* Key Data has correct length.
* Days Since Onset of Symptoms is set and between upper and lower bound.
* ReportType is allowed.
* Starting Interval Number is set and valid.
* Transmission Risk Level is set and between upper and lower bound.
* Rolling Period is set and between upper and lower bound.

Implementation details on validation can be found in: [`ValidFederationKeyFilter.java`](/services/download/src/main/java/app/coronawarn/server/services/download/validation/ValidFederationKeyFilter.java).

### Validation Results and Batch Status

Keys that fail validation will cause a corresponding log entry, but will not hinder batch processing. The FederationBatchStatus indicates if the validation and batch processing was successful.

Processing Status    | Description
---------------------|---------------
PROCESSED            | All DKs passed validation.
PROCESSED_WITH_ERROR | At least one DK failed validation.
ERROR                | Error non-related to DK validation occured.
ERROR_WONT_RETRY     | Error non-related to DK validation occured. Retry of batch processing failed aswell.

## FederationGatewayDownloadService

Handles the actual downloading and parsing of batches.

## BatchDownloadResponse

Java object representing one downloaded and parsed batch.

## FederationKeyNormalizer

Helps to derive Transmission Risk Level from Days Since Onset of Symptoms (for backwards compatability).
Derivation mapping is set in `application.yaml` (under `services`.`download`.`tek-field-derivations`.`trl-from-dsos`).

## RetentionPolicy Runner

See [`PERSISTENCE.md`](/docs/PERSISTENCE.md).
