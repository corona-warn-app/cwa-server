# CWA-Server Submission Service

## Spring Profiles

Spring profiles are used to apply submission service configuration based on the running environment.

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

Below we describe the main steps involved in processing a payload of temporary exposure keys submitted by the mobile client.

## TAN Verification

When submitting diagnosis keys, a Transaction Authorization Number (TAN) token must be present in the request header section (`cwa-authorization`).
Before delegating the TAN validation to the Verification Server, the TAN is verified to be a UUID on the Submission Service side.
Then the TAN token is sent to the [Verification Server](https://github.com/corona-warn-app/cwa-verification-server/blob/master/docs/architecture-overview.md)
to check its validity. If the TAN is valid, then it means it is linked to a valid test.
In case the TAN is not valid, then the verification server will respond with `HTTP 404 Not Found` and the Submission Service will respond with `HTTP 403 Forbidden`.

Implementation details can be found in [`TanVerifier.java`](/services/submission/src/main/java/app/coronawarn/server/services/submission/verification/TanVerifier.java).

## Defaulting values if missing

The contract of the submission service is defined, in part by the [payload protobuf specification](/common/protocols/src/main/proto/app/coronawarn/server/common/protocols/internal/submission_payload.proto). Here you can find all the information (except http headers) that is 
being sent by the mobile client.

The following fields are either defaulted, if not sent by the client, or updated if certain values are missing:
* origin (referring to origin country) is defaulted to a value which is externalized in the `application.yaml/services.submission.payload.default-origin-country`
* visitedCountries list is enhanced to contain the origin country as well in order to have consistency in the distribution logic


## Submission Validations

### Custom Annotation [`@ValidSubmissionPayload`](https://corona-warn-app.github.io/cwa-server/1.0.0/app/coronawarn/server/services/submission/validation/ValidSubmissionPayload.html)

You will find the implementation file at [`/services/submission/src/main/java/app/coronawarn/server/services/submission/validation/ValidSubmissionPayload.java`](/services/submission/src/main/java/app/coronawarn/server/services/submission/validation/ValidSubmissionPayload.java)

### Validation Constraints

Temporary Exposure Keys (TEK's) are submitted by the client device (iOS/Android phone) via the Submission Service.

Some constraints are maintained as enviroment variables which are kept as secrets in the Vault /cwa-server/submission

The constraints put on submitted TEK's are as follows:

* Each TEK contains a `StartIntervalNumber` with the value set at midnight (a datetime e.g. 2nd July 2020 00:00)
* The nuber of TEK's must not exceed the configured maximum number of keys, represented by the `MAX_NUMBER_OF_KEYS` property which is in the vault
* More than one TEK with the same `StartIntervalNumber` may be submitted, these will have their rolling period's combined
* Each TEK must carry either the information of transmission risk level or days since onset of symptoms. If one is missing, the other can be derived. If a TEK is missing both values or contains values which are not in the allowed ranges for each field the payload shall be rejected
* The origin country from the submission payload must be a valid ISO country code and must either be one of the supported countries (maintained in Vault) or it must be empty, in which case it will be defaulted to DE
* The visited countries list from the submission payload must either contain ISO country codes which are part of the supported countries or it must be an empty list, in which case it will be prefilled with the default origin country (e.g. DE)

There are other validations performed prior to persisting keys, which check whether specific fields are in acceptable ranges as defined by the 
system as well as GAEN specification. For this purpose the Java Bean Validation 2.0 framework is used at the entity level. Please see 
[DiagnosisKey](/common/persistence/src/main/java/app/coronawarn/server/common/persistence/domain/DiagnosisKey.java) for the complete list.

## Field Derivations

Starting with version 1.5 the mobile clients stops sending transmission risk level (TRL) values but includes a new field that is used to describe the infectiousness of a person called 'days since onset of symptoms' (DSOS). To ensure backward compatibility with older clients when distriuting keys submitted by newer clients, the server has to derive the missing TRL field from the DSOS value using a mapping maintained in the application configuration. Similarily, to ensure forward compatibility when distributing keys submitted by older clients ( version 1.4 or less), the server
will derive the DSOS from TRL using a reversed internal mapping.

## Diagnosis keys padding

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
