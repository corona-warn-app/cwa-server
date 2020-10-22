# CWA Federation Key Upload Service

This is a spring boot [ApplicationRunner](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/ApplicationRunner.html) service (running as a cronjob). The app will deal with the upload of DE keys to the federation gateway. The job will be configured to run periodically throughout the day to enable keys to be shared with the federation gateway and will run independently from the DE CDN distribution job. This job will source keys from a dedicated table populated with keys which are relevant for sharing. Once a key is published to the gateway via an upload batch it would be removed from this table to ensure it is not resent.

## External Dependencies

- **Vault**: Used for secrets and certificate storage
- **RDBMS**: PostgreSQL as the persistent storage for keys which need to be uploaded
- **Federation Gateway Service**: The service where the service publishes the keys

## Spring Profiles

Spring profiles are used to apply federation key upload service configuration based on the running environment, determined by the active profile.

You will find `.yaml` and `.xml` based profile-specific configuration files at [`/services/upload/src/main/resources`](/services/upload/src/main/resources).

### Available Profiles

Profile                                           | Effect
--------------------------------------------------|-------------
`disable-ssl-client-postgres`                     | Disables SSL with a pinned certificate for the connection to the postgres.
`debug`                                           | Sets the log level to `DEBUG` and changes the `CONSOLE_LOG_PATTERN` used by Log4j 2.
`cloud`                                           | Removes the default values set in `spring.flyway` and `datasource`
`testdata`                                        | Generates test fake keys for upload. Will only generate keys if number of pending keys in DB is less than `services.upload.test-data.max.pending-keys`.

Please refer to the inline comments in the base `application.yaml` configuration file for further details on the configuration properties impacted by the above profiles.

## Data Model

As the upload service will work from a table containing the keys submitted to the CWA Submission service it will have a data model which mirrors the diagnosis key table. This table will be updated in the following cases:

- New keys are submitted to the CWA server where the consentToFederation attribute is provided as true
- Keys will be removed from this table when the distribution run expires keys. This would be relevant in the case that the upload job has not successfully run in some time and we still contain old keys which should be expired.
- Keys will be removed from this table after they have been uploaded successfully to the federation gateway.

## Identification of Keys Applicable for Upload

In order to determine which keys are applicable for upload the following use cases are considered:

- Existing Keys on the CWA Backend Prior to Federation Integration: In this case **NO** existing keys will be shared with the federation gateway. This is due to the fact that these keys are from users which did not have the capability to provide a consent for sharing.
- New Submissions post Federation Integration: This will be determined based on new attributes of the submission payload and these keys are then replicated for use by the upload service. See details under the [Submission Service](./SUBMISSION.md)

## DPP Validations for Publishing

During the upload process it must be ensured that the DPP restrictions which are in place for the normal distribution to the CDN are respected when sending to the federation gateway as much as possible. Therefore, the following validations will be put in place when evaluating keys during the cronjob execution:

- Minimum number of keys required prior to uploading: For distribution this is set via a configuration option found within the distribution service configuration. The similar approach would be leveraged for upload and the values will remain equal. At the time of authoring this it was set to 140 keys minimum.
- Minimum period of time delay post submission of the keys: An expiration period is enforced after the key sets are submitted to the backend prior to them becoming applicable for distribution runs. This is set via configuration found in the distribution service. This same logic will be applied to the keys to send to the federation gateway as well. At the time of authoring this is set to 2 hours.

## Resilience

As this service is a cronjob it will be ensured by the infrastructure that it is running per the schedule defined. Beyond this the following considerations were made to ensure the keys are sent to the federation gateway:

- Duplicated persistent storage for the keys which are required to be sent to the federation gateway. This makes it clear which keys are needing to be sent and as they are persisted if they fail to post in a run they will be re-processed in a future iteration.
- Retry processing around the Federation Gateway API endpoint invocations will be in place in case an initial request fails
- Reprocessing of keys which may have been rejected by the Federation Gateway if applicable

## Security

The means to authenticate with the Federation Gateway is set by the Federation Gateway Service and is described in the associated documentation. To briefly summarize what the upload service will need to ensure:

- POST API calls to the /upload service are secured via mTLS over HTTPs
- Batches which are uploaded are signed by the CWA Server to ensure data integrity per the requirements of the Federation Gateway
- Certificates are managed by an external vault service and consumed from the application at runtime.

### Signing Certificate

Apart from the TLS authentication certificate, the Upload service requires another X.509 certificate for signing Upload batches. The certificate and private key must be provided via environment variables `VAULT_EFGS_BATCHIGNING_CERTIFICATE` and `VAULT_EFGS_BATCHIGNING_SECRET` respectively. The private key encoding type can be specified with environment variable `SIGNATURE_ALGORITHM_NAME` and it's defaulted to `sha256WithRSAEncryption`.

Batch Signature is sent to EFGS on the Request Header `batchSignature`. The signature is a Base64 encoded list of keys (note it's not a `DiagnosisKeyBatch` protobuf encoded object). Keys must be ordered by KeyData and the properties per key must be ordered and separated according to [EFGS signature verification calculation](https://github.com/eu-federation-gateway-service/efgs-federation-gateway/blob/master/docs/software-design-federation-gateway-service.md#32-signature-verification).
