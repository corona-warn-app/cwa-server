# CWA-Server Submission Service

The submission service's only task is to process uploaded diagnosis keys and persist them to the database after the TAN has been verified.
The actual task of the verification is handed over to the verification server, which provides the verification result back to CWA.
After verification was successfully done, the diagnosis keys are persisted in the database, and will be published in the next batch for distribution to the CWA CDN
and to the Federation Gateway for keys that are applicable.

The payload to be sent by the mobile applications is defined in the [submission_payload.proto](../common/protocols/src/main/proto/app/coronawarn/server/common/protocols/internal/submission_payload.proto)

```protobuf
message SubmissionPayload {
  repeated app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey keys = 1;
  optional bytes padding = 2;
  repeated string visitedCountries = 3;
  optional string origin = 4;
  optional app.coronawarn.server.common.protocols.external.exposurenotification.ReportType reportType = 5 [default = CONFIRMED_CLINICAL_DIAGNOSIS];
  optional bool consentToFederation = 6;
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
}
```

Additionally, the endpoint requires the following headers to be set:

```http
CWA-Authorization: TAN <TAN>
CWA-Fake: <0 or 1>
```

There is currently no official specification for publishing diagnosis keys to the server.
Google currently uses the following in their reference implementation.

[exposure_types.go](https://github.com/google/exposure-notifications-server/blob/master/pkg/api/v1alpha1/exposure_types.go)

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

In order to support federation of keys, when new keys are submitted to the CWA Server they will be evaluated to determine if they are applicable for federation. This will be decided based on an attribute provided in the submission payload `optional bool consentToFederation = 6;`. If this is set it means that the user on the mobile device has agreed to have the keys they are submitting sent to the other nations connected to the federation gateway. When processed via submission the keys provided will be duplicated (for a short period) to a table which will be specifically monitored by the Federation Key Upload service. Only keys which are found within this table will be considered for the upload.

## External Dependencies

- **Vault**: Used for secrets and certificate storage
- **RDBMS**: PostgreSQL as the persistent storage for keys which are downloaded

## Spring Profiles

Spring profiles are used to apply submission service configuration based on the running environment, determined by the active profile.

You will find `.yaml` and `.xml` based profile-specific configuration files at [`/services/submission/src/main/resources`](/services/submission/src/main/resources).

### Available Profiles

Profile                                           | Effect
--------------------------------------------------|-------------
`dev`                                             | Sets the log level to `DEBUG` and changes the `CONSOLE_LOG_PATTERN` used by Log4j 2.
`cloud`                                           | Removes default values for the `spring.flyway`, `spring.datasource` and `services.submission.verification.base-url` configurations.
`disable-ssl-server`                              | Disables SSL for the submission endpoint.
`disable-ssl-client-postgres`                     | Disables SSL with a pinned certificate for the connection to the postgres.
`disable-ssl-client-verification`                 | Disables SSL with a pinned certificate for the connection to the verification server.
`disable-ssl-client-verification-verify-hostname` | Disables the verification of the SSL hostname for the connection to the verification server.

Please refer to the inline comments in the base `application.yaml` configuration file for further details on the configuration properties impacted by the above profiles.

## TAN Verification

When submitting diagnosis keys, a Transaction Authorization Number (TAN) token must be present in the request header section (`cwa-authorization`).
Before delegating the TAN validation to the Verification Server, the TAN is verified to be a UUID on the Submission Service side.
Then the TAN token is sent to the [Verification Server](https://github.com/corona-warn-app/cwa-verification-server/blob/master/docs/architecture-overview.md)
to check its validity. If the TAN is valid, then it means it is linked to a valid test.
In case the TAN is not valid, then the verification server will respond with `HTTP 404 Not Found` and the Submission Service will respond with `HTTP 403 Forbidden`.

Implementation details can be found in [`TanVerifier.java`](/services/submission/src/main/java/app/coronawarn/server/services/submission/verification/TanVerifier.java).

## Submission Validations

### Custom Annotation [`@ValidSubmissionPayload`](https://corona-warn-app.github.io/cwa-server/1.0.0/app/coronawarn/server/services/submission/validation/ValidSubmissionPayload.html)

You will find the implementation file at [`/services/submission/src/main/java/app/coronawarn/server/services/submission/validation/ValidSubmissionPayload.java`](/services/submission/src/main/java/app/coronawarn/server/services/submission/validation/ValidSubmissionPayload.java)

### Validation Constraints

Temporary Exposure Keys (TEK's) are submitted by the client device (iOS/Android phone) via the Submission Service.

Constraints maintained as enviroment variables which are present as secrets in the Vault /cwa-server/submission

The constraints put on submitted TEK's are as follows:

* Each TEK contains a `StartIntervalNumber` (a date e.g. 2nd July 2020)
* The period covered by the data file must not exceed the configured maximum number of days, represented by the `MAX_NUMBER_OF_KEYS` property which is in the vault.
* The total combined rolling period for a single TEK cannot exceed maximum rolling period, represented by the `MAX_ROLLING_PERIOD` property which is in the vault.
* More than one TEK with the same `StartIntervalNumber` may be submitted, these will have their rolling period's combined.

### Diagnosis keys padding

To ensure we reach 140 key limit for daily archives and timely distribute the keys, server-side diagnosis keys padding was introduced on CWA server.

For each real key in the payload, submission service will generate additional keys with the same Diagnosis Key data, except the actual byte array that represents the key identifier (KeyData). KeyData for the "fake keys" generated with SecureRandom().nextBytes() as an array of 16 bytes. See [`generateRandomKeyData()`](https://github.com/corona-warn-app/cwa-server/blob/d6edd528e0ea3eafcda26fc7ae6d026fee5b4f0c/services/submission/src/main/java/app/coronawarn/server/services/submission/controller/SubmissionController.java#L73) method.

The number of additionally generated keys is a configurable parameter
See [`random-key-padding-multiplier`](https://github.com/corona-warn-app/cwa-server/blob/d6edd528e0ea3eafcda26fc7ae6d026fee5b4f0c/services/submission/src/main/resources/application.yaml#L23).

    # Example: If the 'random-key-padding-multiplier' parameter is set to 10, and 5 keys are being submitted,
    # then the 5 real submitted keys will be saved to the DB, plus an additional 45 keys with
    # random 'key_data'. All properties, besides the 'key_data', of the additional keys will be
    # identical to the real key.

From a distribution service perspective, generated keys are indistinguishable from the real submitted Diagnosis Keys as these are:

* Based on the real Diagnosis Key data.
* Together cover valid submission key chains (according to the Transmission Risk vector values).

Key padding is implemented in [`SubmissionController`](https://github.com/corona-warn-app/cwa-server/blob/d6edd528e0ea3eafcda26fc7ae6d026fee5b4f0c/services/submission/src/main/java/app/coronawarn/server/services/submission/controller/SubmissionController.java#L203)
and happens after TAN verification, but before we persist sumitted keys on the CWA server.