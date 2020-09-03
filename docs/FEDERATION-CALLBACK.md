# CWA Federation Callback Service

This service will expose an API which is registered with the federation gateways callback service API to accept the callback notifications. The role of this service is to validate the certificates (mTLS handshake) and then store the notification info (batchTag and date) into the postgres database for future processing by the federation key download job.

![Callback Flow Diagram](./images/callback-flow.png)

## External Dependencies

- **Vault**: Used for secrets and certificate storage
- **RDBMS**: PostgreSQL as the persistent storage for notifications

## Spring Profiles

Spring profiles are used to apply federation key download service configuration based on the running environment, determined by the active profile.

You will find `.yaml` and `.xml` based profile-specific configuration files at [`/services/callback/src/main/resources`](/services/callbacksrc/main/resources).

### Available Profiles

Profile                                           | Effect
--------------------------------------------------|-------------
`dev`                                             | Sets the log level to `DEBUG` and changes the `CONSOLE_LOG_PATTERN` used by Log4j 2.
`cloud`                                           | Removes default values for the `spring.flyway`, `spring.datasource` and sets federation gateway contexts
`disable-ssl-server`                              | Disables SSL for the submission endpoint.
`disable-ssl-client-postgres`                     | Disables SSL with a pinned certificate for the connection to the postgres.

Please refer to the inline comments in the base `application.yaml` configuration file for further details on the configuration properties impacted by the above profiles.

## Data Model

The callback service will insert new entries (batch tag and date information) specified within the HTTP request into the table mentioned below. This information will then be further processed by the federation key download job.

```sql
CREATE TABLE federation_batch (
    batch_tag   varchar(20) PRIMARY KEY,
    date        date NOT NULL,
    status      varchar(20)
);
```

## API

TODO