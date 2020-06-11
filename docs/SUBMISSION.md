# CWA-Server Submission Service

## Spring Profiles

Spring profiles are used to apply submission service configuration based on the running environment, determined by the active profile.

You will find `.yaml` and `.xml` based profile-specific configuration files at [`/services/submission/src/main/resources`](/services/submission/src/main/resources). 

### Configuration Properties

Please refer to the inline comments in the `.yaml` configuration files for further details.

### Available Profiles

Profile                   | Effect
--------------------------|-------------
`dev`                     | Sets the log level to `DEBUG` and changes the `CONSOLE_LOG_PATTERN` used by Log4j 2.
`cloud`                   | Removes default values for the `spring.flyway`, `spring.datasource` and `services.submission.verification.base-url` configurations.
`ssl-server`              | Enables SSL for the submission endpoint.
`ssl-client-postgres`     | Enforces SSL with a pinned certificate for the connection to the postgres.
`ssl-client-verification` | Enforces SSL with a pinned certificate for the connection to the verification server.
