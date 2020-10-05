# CWA-Server Submission Service

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
- based on the real Diagnosis Key data
- together cover valid submission key chains (according to the Transmission Risk vector values).

Key padding is implemented in [`SubmissionController`](https://github.com/corona-warn-app/cwa-server/blob/d6edd528e0ea3eafcda26fc7ae6d026fee5b4f0c/services/submission/src/main/java/app/coronawarn/server/services/submission/controller/SubmissionController.java#L203)
and happens after TAN verification, but before we persist sumitted keys on the CWA server.
