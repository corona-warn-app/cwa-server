# CWA-Server Submission Service

The submission service's only task is to process uploaded diagnosis keys and check-ins and persist them to the database after the TAN has been verified.
The actual task of the verification is handed over to the verification server, which provides the verification result back to CWA.
After verification was successfully done, the diagnosis keys are persisted in the database, and will be published in the next batch for distribution to the CWA CDN
and to the Federation Gateway for keys that are applicable.

The payload to be sent by the mobile applications is defined in the [submission_payload.proto](../common/protocols/src/main/proto/app/coronawarn/server/common/protocols/internal/submission_payload.proto) and
[temporary_exposure_key_export.proto](../common/protocols/src/main/proto/app/coronawarn/server/common/protocols/external/exposurenotification/temporary_exposure_key_export.proto) and
[check_in.proto](../common/protocols/src/main/proto/app/coronawarn/server/common/protocols/internal/pt/check_in.proto)
[check_in_protected_report.proto](../common/protocols/src/main/proto/app/coronawarn/server/common/protocols/internal/pt/check_in.proto)

```protobuf
  message SubmissionPayload {
    repeated app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey keys = 1;
    optional bytes requestPadding = 2;
    repeated string visitedCountries = 3;
    optional string origin = 4;
    optional bool consentToFederation = 5;
    repeated app.coronawarn.server.common.protocols.internal.pt.CheckIn checkIns = 6 [deprecated = true];
    optional SubmissionType submissionType = 7 [default = SUBMISSION_TYPE_PCR_TEST];
    repeated app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport checkInProtectedReports = 8;

    enum SubmissionType {
      SUBMISSION_TYPE_PCR_TEST = 0;
      SUBMISSION_TYPE_RAPID_TEST = 1;
    }
  }

  message TemporaryExposureKey {
    // Key of infected user
    optional bytes key_data = 1;
    // Varying risk associated with a key depending on diagnosis method
    optional int32 transmission_risk_level = 2;
    // The interval number since epoch for which a key starts
    optional int32 rolling_start_interval_number = 3;
    // Increments of 10 minutes describing how long a key is valid
    optional int32 rolling_period = 4
        [default = 144]; // defaults to 24 hours
    // Type of diagnosis associated with a key.
    optional ReportType report_type = 5 [default = CONFIRMED_TEST];
    // Number of days elapsed between symptom onset and the TEK being used.
    // E.g. 2 means TEK is 2 days after onset of symptoms.
    optional sint32 days_since_onset_of_symptoms = 6;
  }

  message CheckIn {
    bytes locationId = 1;
    uint32 startIntervalNumber = 2;
    uint32 endIntervalNumber = 3;
    uint32 transmissionRiskLevel = 4;
  }

  message CheckInProtectedReport {
    bytes locationIdHash = 1;
    bytes iv = 2;
    bytes encryptedCheckInRecord = 3;
    bytes mac = 4;
}
```

Additionally, the endpoint requires the following headers to be set:

```http
CWA-Authorization: TAN <TAN>
CWA-Fake: <0 or 1>
```

The response headers returned by this endpoint include the number of check-ins that were successfully saved and the number of check-ins that were filtered out before further processing.

```http response headers
cwa-filtered-checkins: "number" <String>
cwa-saved-checkins: "number" <String>
```

There is currently no official specification for publishing diagnosis keys to the server.
Google currently uses the following in their reference implementation.

[exposure_types.go](https://github.com/google/exposure-notifications-server/blob/HEAD/pkg/api/v1alpha1/exposure_types.go)

```golang
type Publish struct {
  Keys                []ExposureKey `json:"temporaryExposureKeys"`
  Regions             []string      `json:"regions"`
  AppPackageName      string        `json:"appPackageName"`
  VerificationPayload string        `json:"verificationPayload"`
  HMACKey             string        `json:"hmackey"`
  Padding             string        `json:"padding"`

  Platform                  string `json:"platform"`                  // DEPRECATED
  DeviceVerificationPayload string `json:"deviceVerificationPayload"` // DEPRECATED
}
```

Due to concerns regarding data privacy and protection, device attestation is currently not being used by CWA.

In order to support federation of keys, when new keys are submitted to the CWA Server they will be evaluated to determine if they are applicable for federation. This will be decided based on an attribute provided in the submission payload `optional bool consentToFederation = 6;`. If this is set it means that the user on the mobile device has agreed to have the keys they are submitting sent to the other nations connected to the federation gateway. When processed via submission the keys provided will be duplicated (for a short period) to a table which will be specifically monitored by the Federation Key Upload service. Only keys which are found within this table will be considered for the upload. This is managed via triggers within the PostgreSQL database.

## External Dependencies

- **Vault**: Used for secrets and certificate storage
- **RDBMS**: PostgreSQL as the persistent storage for keys which are downloaded

## Spring Profiles

Spring profiles are used to apply submission service configuration based on the running environment.

You will find `.yaml` and `.xml` based profile-specific configuration files at [`/services/submission/src/main/resources`](/services/submission/src/main/resources).

### Available Profiles

Profile                                           | Effect
--------------------------------------------------|-------------
`debug`                                           | Sets the log level to `DEBUG` and changes the `CONSOLE_LOG_PATTERN` used by Log4j 2.
`cloud`                                           | Removes default values for the `spring.flyway`, `spring.datasource` and `services.submission.verification.base-url` configurations.
`disable-ssl-client-postgres`                     | Disables SSL with a pinned certificate for the connection to the postgres.

Please refer to the inline comments in the base `application.yaml` configuration file for further details on the configuration properties impacted by the above profiles.

Below we describe the main steps involved in processing a payload of temporary exposure keys submitted by the mobile client.

## TAN Verification

When submitting diagnosis keys, a Transaction Authorization Number (TAN) token must be present in the request header section (`cwa-authorization`).
Before delegating the TAN validation to the Verification Server, the TAN is verified to be a UUID on the Submission Service side.
Then the TAN token is sent to the [Verification Server](https://github.com/corona-warn-app/cwa-verification-server/blob/HEAD/docs/architecture-overview.md)
to check its validity. If the TAN is valid, then it means it is linked to a valid test.
In case the TAN is not valid, then the verification server will respond with `HTTP 404 Not Found` and the Submission Service will respond with `HTTP 403 Forbidden`.

Implementation details can be found in [`TanVerifier.java`](/services/submission/src/main/java/app/coronawarn/server/services/submission/verification/TanVerifier.java).

## Defaulting values if missing

The contract of the submission service is defined, in part, by the [payload protobuf specification](/common/protocols/src/main/proto/app/coronawarn/server/common/protocols/internal/submission_payload.proto). Here you can find all the information (except http headers) that is being sent by the mobile client.

The following fields are either defaulted, if not sent by the client, or updated if certain values are missing:

- origin (referring to origin country) is defaulted to a value which is externalized in the `application.yaml/services.submission.payload.default-origin-country`
- visitedCountries list is enhanced to contain the origin country as well in order to have consistency in the distribution logic

## Submission Validations

### Custom Annotation [`@ValidSubmissionPayload`](https://corona-warn-app.github.io/cwa-server/1.0.0/app/coronawarn/server/services/submission/validation/ValidSubmissionPayload.html)

You will find the implementation file at [`/services/submission/src/main/java/app/coronawarn/server/services/submission/validation/ValidSubmissionPayload.java`](/services/submission/src/main/java/app/coronawarn/server/services/submission/validation/ValidSubmissionPayload.java)

### Validation Constraints

Temporary Exposure Keys (TEK's) are submitted by the client device (iOS/Android phone) via the Submission Service.

Some constraints are maintained as enviroment variables and kept in Vault under the key path: /cwa-server/submission

The constraints put on submitted TEK's are as follows:

- Each TEK contains a `StartIntervalNumber` with the value set at midnight (a datetime e.g. 2nd July 2020 00:00)
- The number of TEK's must not exceed the configured maximum number of keys, represented by the `MAX_NUMBER_OF_KEYS` property which is in the vault
- More than one TEK with the same `StartIntervalNumber` may be submitted, these will have their rolling period's combined
- Each TEK must carry either the information of transmission risk level or days since onset of symptoms. If one is missing, the other can be derived. If a TEK is missing both values or contains values which are not in the allowed ranges for each field the payload shall be rejected
- The origin country from the submission payload must be a valid ISO country code and must either be one of the supported countries (maintained in Vault) or it must be empty, in which case it will be defaulted to DE
- The visited countries list from the submission payload must either contain ISO country codes which are part of the supported countries or it must be an empty list, in which case it will be prefilled with the default origin country (e.g. DE)

There are other validations performed prior to persisting keys, which check whether specific fields are in acceptable ranges as defined by the system as well as GAEN specification. For this purpose the Java Bean Validation 2.0 framework is used at the entity level. Please see [DiagnosisKey](/common/persistence/src/main/java/app/coronawarn/server/common/persistence/domain/DiagnosisKey.java) for the complete list.

## Field Derivations

Starting with version 1.5 the mobile client stops sending transmission risk level (TRL) values but includes a new field that is used to describe the infectiousness of a person called 'days since onset of symptoms' (DSOS). To ensure backward compatibility with older clients when distributing keys submitted by newer clients, the server has to derive the missing TRL field from the DSOS value using a mapping maintained in the application configuration. Similarly, to ensure forward compatibility when distributing keys submitted by older clients (version 1.4 or less), the server
will derive the DSOS from TRL using a reversed internal mapping.

### Diagnosis keys padding

To ensure we reach 140 key limit for daily archives and timely distribute the keys, server-side diagnosis keys padding was introduced on CWA server.

For each real key in the payload, submission service will generate additional keys with the same Diagnosis Key data, except the actual byte array that represents the key identifier (KeyData). KeyData for the "fake keys" generated with SecureRandom().nextBytes() as an array of 16 bytes. See [`generateRandomKeyData()`](https://github.com/corona-warn-app/cwa-server/blob/d6edd528e0ea3eafcda26fc7ae6d026fee5b4f0c/services/submission/src/main/java/app/coronawarn/server/services/submission/controller/SubmissionController.java#L73) method.

The number of additionally generated keys is a configurable parameter
See [`random-key-padding-multiplier`](https://github.com/corona-warn-app/cwa-server/blob/d6edd528e0ea3eafcda26fc7ae6d026fee5b4f0c/services/submission/src/main/resources/application.yaml#L23).

```yaml
  # Example: If the 'random-key-padding-multiplier' parameter is set to 10, and 5 keys are being submitted,
  # then the 5 real submitted keys will be saved to the DB, plus an additional 45 keys with
  # random 'key_data'. All properties, besides the 'key_data', of the additional keys will be
  # identical to the real key.
```

From a distribution service perspective, generated keys are indistinguishable from the real submitted Diagnosis Keys as these are:

- Based on the real Diagnosis Key data.
- Together cover valid submission key chains (according to the Transmission Risk vector values).

Key padding is implemented in [`SubmissionController`](https://github.com/corona-warn-app/cwa-server/blob/d6edd528e0ea3eafcda26fc7ae6d026fee5b4f0c/services/submission/src/main/java/app/coronawarn/server/services/submission/controller/SubmissionController.java#L203)
and happens after TAN verification, but before we persist sumitted keys on the CWA server.

## Submission Application Properties

Starting with version 2.8 there is now the possibility to submit encrypted check-ins. For this a new property ```unencrypted-checkins-enabled```is included.
This flag is used to control whether check-ins ( which is the current default) are still allowed or not accepted anymore.

```yaml
unencrypted-checkins-enabled: <true or false>
```

### Additional Note

**Not to be confused** with [```EVREG_UNENCRYPTED_CHECKINS_ENABLED```](../services/distribution/src/main/resources/application.yaml) from the distribution service which indicates for the mobile clients which feature is enabled and is of type ```Integer```.
