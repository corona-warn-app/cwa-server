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

* `StartIntervalNumber` values from the same [`SubmissionPayload`](https://corona-warn-app.github.io/cwa-server/1.0.0/app/coronawarn/server/common/protocols/internal/SubmissionPayload.html) shall be unique.
* There must not be any keys in the [`SubmissionPayload`](https://corona-warn-app.github.io/cwa-server/1.0.0/app/coronawarn/server/common/protocols/internal/SubmissionPayload.html) that have overlapping time windows.
* The period covered by the data file must not exceed the configured maximum number of days, which is defined by `max-number-of-keys` property in [`application.yaml`](/services/submission/src/main/resources/application.yaml). Currently no submissions with more than 14 keys are accepted
* Visited Counties: TODO: Align with team as current implementation might not be sufficient. 

## Data Derivations & Defaults

To support integration with the Federation Gateway some attributes are placed into the Submission Proto as this information is required to be passed along. The attributes are optional and if no provided may have information derived and defaulted per the blow:

- **Visited Countries**: This is used for the user to indicate what countries they had visited prior to a positive test result. TODO: Validate the Other Countries scenario as in this case we would default essentially all country codes.
- **Consent To Federation**: This is used in order to later determine if the associated keys (and generated padded keys) should be uploaded to the federation gateway. If consent is not provided by the user this is be default set to false.
- **Origin Country**: This is used by the federation gateway to know the source country for the key data. As CWA is intended for use within DE this value is defaulted to DE
- **Report Type**: Indicates how the test/verification of COVID-19 was confirmed. For the CWA app `CONFIRMED_CLINICAL_DIAGNOSIS` is defaulted as this is the only way submissions can be made at this time.
- **Days Since onset of Symptoms**: 
