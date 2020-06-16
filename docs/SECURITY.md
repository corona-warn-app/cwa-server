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

Apart from those configuration options, the `dev` profile will never be used in production to prevent leakage of data for security and privacy concerns reasons.
