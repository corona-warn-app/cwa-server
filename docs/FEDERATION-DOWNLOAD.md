# CWA Federation Key Download Service

This is a spring boot [ApplicationRunner](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/ApplicationRunner.html) service (running as a cronjob). The app will deal with the download, sematic validation, extraction, and storage of the keys from the federation gateway. The download service leverages the download API of the federation gateway and will trigger downloads of available batches provided by the Callback Service.

In addition to callback-based downloads, the Download Service supports date-based downloads.
For that, the download service will use the polling mechanism provided by the federation gateway based on `batchTag` and `date` combinations and it will keep track of its last processed state within the database. The polling mechanism would mainly be used for mass loading scenarios.

On the download of keys from the federation gateway a process to derive a TRL from the DSOS needs to take place. This is done to enable the keys to be consumable by the DE CWA app as not all countries support the same approach which is required for the CWA app. The means the following:

- For keys from countries which support the daysSinceOnset scenario will need to be converted into an appropriate transmission risk level based on values defined in configuration
- For keys where we have no DSOS mapping configuration there will be a need to provide a reasonable global default otherwise we would need to reject the key download.

Refer to the configuration settings defined in [derivation-maps.yaml](../common/persistence/src/main/resources/derivation-maps.yaml)

These rules will allow the keys sourced from the federation gateway to be processed within the CWA App and be considered with the risk detection algorithms.

## External Dependencies

- **Vault**: Used for secrets and certificate storage
- **RDBMS**: PostgreSQL as the persistent storage for keys which are downloaded
- **Federation Gateway Service**: The service where the service downloads the keys

## Spring Profiles

Spring profiles are used to apply federation key download service configuration based on the running environment, determined by the active profile.

You will find `.yaml` and `.xml` based profile-specific configuration files at [`/services/federation-download/src/main/resources`](/services/federation-download/src/main/resources).

### Available Profiles

Profile                                           | Effect
--------------------------------------------------|-------------
`debug`                                           | Sets the log level to `DEBUG` and changes the `CONSOLE_LOG_PATTERN` used by Log4j 2.
`cloud`                                           | Removes default values for the `spring.flyway`, `spring.datasource` and `services.submission.verification.base-url` configurations.
`disable-ssl-client-postgres`                     | Disables SSL with a pinned certificate for the connection to the postgres.

Please refer to the inline comments in the base `application.yaml` configuration file for further details on the configuration properties impacted by the above profiles.

## Environment Variables

Download specific environment variables:

Variable Name                      | Default Value                                | Description
-----------------------------------|----------------------------------------------|-------------
EFGS_ENFORCE_DATE_BASED_DOWNLOAD   | false                                        | Enable date-based download
EFGS_ENFORCE_DOWNLOAD_OFFSET_DAYS  | 0                                            | The offset in days for which the keys shall be downloaded (must be in range 0 - 14).
ALLOWED_REPORT_TYPES_TO_DOWNLOAD   | CONFIRMED_TEST, CONFIRMED_CLINICAL_DIAGNOSIS | Accepted ReportTypes for download.

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
allowed-report-types | CONFIRMED_TEST | Accepted ReportTypes (comma separated list).
min-dsos             | -14            | Accepted lower bound for Days Since Onset of Symptoms.
max-dsos             | 4000           | Accepted upper bound for Days Since Onset of Symptoms.
min-rolling-period   | 0              | Accepted lower bound for Rolling Period.
max-rolling-period   | 144            | Accepted upper bound for Rolling Period.
min-trl              | 1              | Accepted lower bound for Transmission Risk Level.
max-trl              | 8              | Accepted upper bound for Transmission Risk Level.

### Validation Checks

The checks performed on downloaded DKs are as follows:

- Key Data has correct length.
- ReportType is allowed.
- Rolling Period is between upper and lower bound.
- Starting Interval Number is valid.
- Days Since Onset of Symptoms (DSOS) is between upper and lower bound.
- Transmission Risk Level (TRL) is between upper and lower bound.
If the validation of DSOS or TRL fails, the corresponding value is derived from the other.

Diagnosis Keys are only rejected if both values are missing or invalid.

Implementation details on validation can be found in: [`ValidFederationKeyFilter.java`](/services/download/src/main/java/app/coronawarn/server/services/download/validation/ValidFederationKeyFilter.java).

### Validation Results and Batch Status

Keys that fail validation will cause a corresponding log entry, but will not hinder batch processing. The FederationBatchStatus indicates if the validation and batch processing was successful.

Processing Status    | Description
---------------------|---------------
PROCESSED            | All DKs passed validation.
PROCESSED_WITH_ERROR | At least one DK failed validation.
ERROR                | Error non-related to DK validation occurred.
ERROR_WONT_RETRY     | Error non-related to DK validation occurred. Retry of batch processing failed as well.

## FederationGatewayDownloadService

Handles the actual downloading and parsing of batches.

## BatchDownloadResponse

Java object representing one downloaded and parsed batch.

## FederationKeyNormalizer

Helps to derive Transmission Risk Level from Days Since Onset of Symptoms (for backwards compatability).
Derivation mapping is set in `application.yaml` (under `services`.`download`.`tek-field-derivations`.`trl-from-dsos`).

## RetentionPolicy Runner

See [`PERSISTENCE.md`](/docs/PERSISTENCE.md).

## Resilience

As this service is a cronjob it will be ensured by the infrastructure that it is running per the schedule defined. Beyond this the following considerations were made to ensure the keys are sent to the federation gateway:

- Batches which need to be processed are known on the DB. If a failure occurs the next run will pickup from where it left off ensuring we do not miss any batch requests.
- Retry processing around the Federation Gateway API endpoint invocations will be in place in case an initial request fails

## Security

The means to authenticate with the Federation Gateway is set by the Federation Gateway Service and is described in the associated documentation. To briefly summarize:

- GET API calls to the /download service are secured via mTLS over HTTPs
- Batches which are downloaded are signed by the National Backend Servers to ensure data integrity per the requirements of the Federation Gateway. The download service will make use of the audit API of the federation gateway and the provided public keys to verify that the keys we are receiving are genuine. Logs will also be in place to denote that a batch with tag X was received from gateway and when the audit validations are in place logs will be generated for each batch/country combination. Note: Use of the Audit API is still in planning stages
- Certificates are managed by an external service and consumed from the application at runtime.

## Batch Auditing

Note: Use of the Audit API is still in planning stages

The Federation Gateway provides an API to request audit information in relation to a batch. This audit operation provides the possibility to verify data integrity and authenticity within a batch. The operation returns information about the batch, for instance:

- Countries contained in the batch
- Amount of keys
- Batch signatures by country
- Uploading Information
- Signature Information
- Operator Signatures

All this information can be cross-checked over the certificate authorities or over the transmitted certificate information. (in the case of a self-signed certificate).

The download service will make use of this Audit API in order to ensure the data integrity from the countries received with a batch. The service will prepare a hash based on the keys associated with a particular country and compare this with what is provided from the audit API. If the validation fails the keys from this country will not be accepted into the CWA Server database. Audit logs will be maintained which indicates that keys from country X were rejected due to the failure of the audit verification.
