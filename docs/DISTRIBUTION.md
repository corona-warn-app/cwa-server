# CWA-Server Distribution Service

## Spring Profiles

Spring profiles are used to apply distribution service configuration based on the running environment, determined by the active profile.

You will find `.yaml` and `.xml` based profile-specific configuration files at [`/services/distribution/src/main/resources`](/services/distribution/src/main/resources).

### Available Profiles

Profile                       | Effect
------------------------------|-------------
`dev`                         | Sets the log level to `DEBUG` and changes the `CONSOLE_LOG_PATTERN` used by Log4j 2.
`cloud`                       | Removes default values for the `spring.flyway`, `spring.datasource` and `services.distribution.objectstore` configurations. <br>Changes the distribution output path and turns off `set-public-read-acl-on-put-object`.
`demo`                        | Includes incomplete days and hours into the distribution run, thus creating aggregates for the current day and the current hour (and including both in the respective indices). When running multiple distributions in one hour with this profile, the date aggregate for today and the hours aggregate for the current hour will be updated and overwritten. This profile also turns off the expiry policy (Keys must be expired for at least 2 hours before distribution) and the shifting policy (there must be at least 140 keys in a distribution).
`testdata`                    | Causes test data to be inserted into the database before each distribution run. By default, around 1000 random diagnosis keys will be generated per hour. If there are no diagnosis keys in the database yet, random keys will be generated for every hour from the beginning of the retention period (14 days ago at 00:00 UTC) until one hour before the present hour. If there are already keys in the database, the random keys will be generated for every hour from the latest diagnosis key in the database (by submission timestamp) until one hour before the present hour (or none at all, if the latest diagnosis key in the database was submitted one hour ago or later).
`signature-dev`               | Sets the app package ID in the export packages' signature info to `de.rki.coronawarnapp-dev` so that the non-productive/test public key will be used for client-side validation.
`signature-prod`              | Sets the app package ID in the export packages' signature info to `de.rki.coronawarnapp` so that the productive public key will be used for client-side validation.
`disable-ssl-client-postgres` | Disables SSL for the connection to the postgres database.

Please refer to the section [Configuration Properties](#configuration-properties) and the inline comments in the base `application.yaml` configuration file for further details on the configuration properties impacted by the above profiles.

## Configuration Properties

All of the configurable parameters, that are used throughout this service can be found in the
[`application.yaml`](/services/distribution/src/main/resources/application.yaml) file. This configuration file is
divided into different sub-categories:

- Some general configuration, which isn't divided into sub-categories
- `paths` - Local paths, that are used during the export creation
- `tek-export` - Configuration for the exported archive, that is saved on the S3-compatible storage
- `api` - API configuration, configures the API, which is used by the mobile app to query diagnosis keys
- `signature` - Signature configuration, used for signing the exports
- `objectstore` - Configuration for the S3 compatible object storage

## Object Store

The communication with the S3 compatible object storage, that is hosted by Deutsche Telekom is achieved through AWS SDK
v2. The files, that will be uploaded to the S3 compatible storage are created on local storage first (output path
defined in application configuration) and are then uploaded to the storage provider.

### Headers

Up to three headers are set during upload, depending on the configuration properties.

#### `Cache-Control`

Defines the maximum amount of time a published resource is considered fresh, when held in cache. Will be set for each
upload. The default value is 300 seconds.

#### `x-amz-acl`

Defines the canned ACL for the uploaded file. Is only set if [`set-public-read-acl-on-put-object`] is set to true in the
configuration properties. Will be `public-read` in that case, which grants full control to the owners and read-access to
AllUsers. This setting should only be used when running with the application with the local Zenko Cloudserver.

#### `cwa-hash`

Custom header, which adds the MD5 hash of the archive contents (export.bin). If the file isn't an archive, the contents
of the file will be used to build the hash (index files). This header is used to determine, whether a file should be
uploaded or not. If the hash for the file is the same as the hash available on the S3 compatible storage it will not be
uploaded, since the contents of that specific file did not change, so there is no need to re-upload the file. If the
hash differs, or the file is not available on the S3 compatible storage the file will be uploaded.

This header is needed, since it is not possible to create byte-identical archives when using ECDSA due to its
non-deterministic nature.

### Threading

The upload operations are being run in multiple threads in order to increase performance. The number of threads is
defined in the application configuration. Each upload operation is passed to Spring Boot's `ThreadPoolTaskExecutor`,
which then distributes them across the available threads. Once all tasks are submitted, the logic checks, whether all
threads have terminated before shutting down the thread pool. If errors are thrown, they are handled as explained in the
following section.

### Error Handling

In order to increase resilience of the distribution service two error handling measures were introduced.

The first one being Spring Boot's Retry logic, which is applied to all S3 operations with the number of retries being
specified in the application configuration. This results in Spring Boot retrying the operation up to three times, with a
two second pause in between.

The second part will catch operations, that have failed even after retrying them through SpringBoot's Retry logic. If
more than five uploads fail (can be configured in the application configuration) the program will terminate with an
error, if less than five operations have failed so far the error will just be logged to console, but the upload will
continue.

The error handling is designed to handle intermediate errors, like short connection problems. If too many operations
fail it is safe to assume, that a bigger problem is occurring and that subsequent operations will also fail. In this
case the program is terminated to prevent unnecessary load.

### Retention

The same 14 days retention period (like the database) is also enforced on the S3 compatible storage. Each distribution
run will execute the retention policy.

When data deletion is executed on the object store, the object store is instructed to delete all files with the following
prefix:

`version/v1/diagnosis-keys/country/DE/<date>`

In which `<date>` stands for the ISO formatted date (e.g. `2012-06-05`), and is before the retention cutoff date
(today - 14 days).

## Assembly Process

The exported diagnosis-keys are being organized in hourly and daily archives. The folder structure is as follows:
`/version/<version>/diagnosis-keys/country/<ISO-3166-country>/date/<YYYY-MM-DD>/hour/<hh>/index`. The version, country,
date and hour directory also contain an index file each, listing all the subdirectories. All generated files are named
`index`, which acts as a workaround to also support files & folders with the same name - e.g.
`version/v1/diagnosis-keys/country` is both a file and the folder `version/v1/diagnosis-keys/country/`. S3 supports this
concept, but writing this structure to the local disk is not supported. Therefore, this country file will be assembled as
`version/v1/diagnosis-keys/country/index`. The `/index` part will then be removed upon S3 upload.

For each assembly run the diagnosis keys for the last 14 days are queried. Based on the result, hour and their parent
directories are created and the keys are added to their respective archives. To which archive the key should be added is
determined by the distribution timestamp.

The diagnosis keys need to be expired for at least two hours, before they can be distributed. This means that if the key
has been submitted within two hours after the expiry date, it cannot be published immediately. Therefore a distribution
timestamp is calculated, which is either the submission timestamp, or, if the submission timestamp is within two hours
after the expiry date, the expiry date plus two hours. This ensures compliance with the specification from Google and
Apple.

Each run creates all hourly and daily archives for the last 14 days. To prevent unnecessary uploads the
[`cwa-hash`](#cwa-hash) header has been introduced.

## Diagnosis Key Export Files

Coming soon

## Signing

Coming soon

## Bundling and Shifting

Coming soon
