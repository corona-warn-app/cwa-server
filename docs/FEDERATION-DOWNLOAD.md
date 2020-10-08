# CWA Federation Key Download Service

This is a spring boot [ApplicationRunner](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/ApplicationRunner.html) service (running as a cronjob). The app will deal with the download, sematic validation, extraction, and storage of the keys from the federation gateway. The download service leverages the download API of the federation gateway and will trigger downloads of available batches since the last time it executed. For the initial release the download service will use the polling mechanism provided by the federation gateway based on `batchTag` and `date` combinations and it will keep track of its last processed state within the database. When and if the callback service integration is fully realized, the polling mechanism would mainly be used for mass loading scenarios, and this service will then only download the persisted individual batches where notifications have been received.

On the download of keys from the federation gateway a process of normalization needs to take place. This is done to enable the keys to be consumable by the DE CWA app as not all countries support the same approach which is required for the CWA app. The means the following:

- For keys from countries which support the daysSinceOnset scenario will need to be converted into an appropriate transmission risk level
- For keys from countries which support the transmission risk level we might need to translate the value to something which is reasonable for the RKI
- For keys from countries which support neither, or a key doesn't happen to have either value then there will be a need to provide a reasonable default

The rules above would be defined at 2 levels:

- On a per country basis such that the rules can be specified and values derived based on the origin country
- At a global level for situations where we do not have country specific rules

They would also take into consideration other attributes provided within the key data for example report type. The full set of attributes to be evaluated and how is still TBD.

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
`dev`                                             | Sets the log level to `DEBUG` and changes the `CONSOLE_LOG_PATTERN` used by Log4j 2.
`cloud`                                           | Removes default values for the `spring.flyway`, `spring.datasource` and sets federation gateway contexts
`disable-ssl-server`                              | Disables SSL for the submission endpoint.
`disable-ssl-client-postgres`                     | Disables SSL with a pinned certificate for the connection to the postgres.

Please refer to the inline comments in the base `application.yaml` configuration file for further details on the configuration properties impacted by the above profiles.

## Data Model

This service doesn't specifically introduce any new data model concepts. It will reuse the existing diagnosis key table where it will store the keys that it downloads.

```sql
    CREATE TABLE diagnosis_key (
        key_data bytea PRIMARY KEY,
        rolling_period integer NOT NULL,
        rolling_start_interval_number integer NOT NULL,
        submission_timestamp bigint NOT NULL,
        transmission_risk_level integer NOT NULL,
        consent_to_federation boolean NOT NULL DEFAULT FALSE,
        origin_country varchar (2),
        visited_countries varchar (2) [],
        verification_type varchar(20)
        efgs_batch_tag text -> TODO: Check with team on adding this attribute
);
```

## Resilience

As this service is a cronjob it will be ensured by the infrastructure that it is running per the schedule defined. Beyond this the following considerations were made to ensure the keys are sent to the federation gateway:

- Batches which need to be processed are known on the DB. If a failure occurs the next run will pickup from where it left off ensuring we do not miss any batch requests.
- Retry processing around the Federation Gateway API endpoint invocations will be in place incase an initial request fails

## Security

The means to authenticate with the Federation Gateway is set by the Federation Gateway Service and is described in the associated documentation. To briefly summarize what the upload service will need to ensure:

- GET API calls to the /download service are secured via mTLS over HTTPs
- Batches which are download are signed by the National Backend Servers to ensure data integrity per the requirements of the Federation Gateway. The download service will make use of the audit API of the federation gateway and the provided public keys to verify that the keys we are receiving are genuine. Logs will also be in place to denote that a batch with tag X was received from gateway and when the audit validations are in place logs will be generated for each batch/country combination.
- Certificates are managed by an external vault service and consumed from the application at runtime.

## Batch Auditing

To be implemented/aligned:

The Federation Gateway provides an API to request audit information in relation to a batch. This audit operation provides the possibility to verify data integrity and authenticity within a batch. The operation returns information about the batch, for instance:

- Countries contained in the batch
- Amount of keys
- Batch signatures by country
- Uploading Information
- Signature Information
- Operator Signatures

All this information can be cross-checked over the certificate authorities or over the transmitted certificate information. (in the case of a self-signed certificate).

The download service will make use of this Audit API in order to ensure the data integrity from the countries received with a batch. The service will prepare a hash based on the keys associated with a particular country and compare this with what is provided from the audit API. If the validation fails the keys from this country will not be accepted into the CWA Server database. Audit logs will be maintained which indicates that keys from country X were rejected due to the failure of the audit verification.
