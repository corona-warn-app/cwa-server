# CWA-Server Security

## TLS Configuration

The cwa-server uses encrypted connections for external and internal communication. The main configuration for the external communication channel can be found [here](../services/submission/src/main/resources/application.yaml) (see section `server`). The current settings will only allow TLS version 1.2 and 1.3. The offered ciphers are chosen in accordance to the recommendations in [BSI-TR-02102-2](https://www.bsi.bund.de/SharedDocs/Downloads/DE/BSI/Publikationen/TechnischeRichtlinien/TR02102/BSI-TR-02102-2.pdf).
Internal communication is configured for instance [in the feign client](../services/submission/src/main/java/app/coronawarn/server/services/submission/verification/CloudFeignClientProvider.java).
Database connections are established via TLS and are enabled for mutual authentication, see [here](../services/submission/src/main/resources/application.yaml#L38).

## Security-Related Spring Profiles

The TLS configuration previously described can be changed to enable certain test scenarios. It is important to realize that this degradation of security is only possible by setting a Spring profile, while the default behavior will always use the most secure configuration. To prevent misconfiguration in production, multiple controls are implemented. This includes among others configuration-as-code deployments, explicit log messages for unsafe configurations and monitoring of deployed configurations.
The relevant profiles are:

|Profile|Description|
|-------|-----------|
|`disable-ssl-server`|Disables SSL for the submission endpoint.|
|`disable-ssl-client-postgres`|Disables SSL with a trusted certificate for the connection to the postgres database.|
|`disable-ssl-client-verification`|Disables SSL with a trusted certificate for the connection to the verification server.|
|`disable-ssl-client-verification-verify-hostname`|Disables the verification of the SSL hostname for the connection to the verification server.|

Apart from those configuration options, the `debug` profile will never be used in production to prevent leakage of data for security and privacy concerns reasons.

## Plausible Deniability

### Fake Requests

Fake (or "dummy") requests are sent by the mobile devices in randomized intervals. These requests do not trigger any server-side processing or storage and simply result in an HTTP response after an dynamically calculated delay.
The fake requests are intended to prevent an attacker from using network and traffic analysis to find out whether a user was actually tested positive and is now submitting his keys.

The implementation details on fake request handling can be found here [FakeRequestController](../services/submission/src/main/java/app/coronawarn/server/services/submission/controller/FakeRequestController.java).

### Payload Padding

The mobile apps are collecting keys based on their install date. When a user uploads his keys (after being tested positive), the payload will normally contain 1-13 keys. Although the traffic between mobile & server is secured, an attacker may still sniff the packages in the network and predict, based on the request size, how many keys are probably part of the submission request. This could lead to additional information for the attacker in an attempt to deanonymize a user.

In order to mitigate this kind of information leakage, the submission payload contains padding. The padding hides the true size of the request - making it difficult for an adversary to extract knowledge.

The Padding is specified in the corresponding [Protocol](../common/protocols/src/main/proto/app/coronawarn/server/common/protocols/internal/submission_payload.proto).

The Submission Service ensures that padded Payloads do not exceed the maximum request size.
Implementation details can be found [here](../services/submission/src/main/java/app/coronawarn/server/services/submission/config/SubmissionPayloadSizeFilter.java)
