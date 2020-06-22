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
