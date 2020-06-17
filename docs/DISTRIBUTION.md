# CWA-Server Distribution Service

## Configuration Properties

All of the configurable parameters, that are used throughout this service can be found in the [`application.yaml`](/services/distribution/src/main/resources/application.yaml) file.
This configuration file is divided into different sub-categories:

- Some general configuration, which isn't divided into sub-categories
- `paths` - Local Paths, that are used during the export creation
- `tek-export` - Configuration for the exported archive, that is saved on the S3-compatible storage
- `api` - API Configuration, configures the API, which is used by the mobile app to query diagnosis keys
- `signature` - Signature Configuration, used for signing the exports
- `objectstore` - Configuration for the S3 compatible object storage

## Object Store

The communication with the S3 compatible object storage, that is hosted by Deutsche Telekom is done through the AWS SDK
v2. The files, that will be updated to the S3 compatible storage are created on local storage first (output path defined
in application configuration) and are then uploaded to the storage provider.

### Headers

Up to three headers are set during upload, depending on the configuration properties.

#### `Cache-Control`

Defines the maximum amount of time a published resource is considered fresh, when held in cache. Will be set for each
upload, default is 300 seconds.

#### `x-amz-acl`

Defines the canned ACL for the uploaded file. Is only set if [`set-public-read-acl-on-put-object`] is set to true in the
configuration properties. Will be `public-read` in that case, which grants full control to the Owners and read-access to
AllUsers.

#### `cwa-hash`

Custom header, which adds the SHA-256 hash of the archive contents. This header is used to determine, whether a file
should be uploaded or not. If the hash for the file is the same as the hash available on the S3 compatible storage it
will not be uploaded, since the contents of that specific file did not change, so there is no need to re-upload the
file. If the hash differs, or the file is not available on the S3 compatible storage the file will be uploaded.

This header is needed, since it is not possible to create byte-identical archives when using ECDSA, since it is non-deterministic.

### Threading

The upload operations are being run in multiple threads to increase performance. The number of threads is defined in the
application configuration. SpringBoot's `ThreadPoolTaskExecutor` is used to create and manage the threads. Each upload
operation will be submitted as a single task, which then will be distributed across the available threads. Once all
threads are submitted, the logic checks, whether all threads are finished before shutting down the Thread Pool. If
errors are thrown, they are handled as explained in the next section.

### Error Handling

To make the distribution service as resilient as possible two error handling measures were introduced.

The first one being SpringBoot's Retry logic, which is applied to all S3 operations with the number of retries being
specified in the application configuration. This results in SpringBoot retrying the operation up to three times, with a
two second pause in between.

The second part will catch operations, that have failed even after retrying them through SpringBoot's Retry logic. If
more than five uploads fail (can be configured in the application configuration) the program will terminate with an
error, if less than five operations have failed so far the error will just be logged to console, but the upload will
continue.

The error handling is designed to handle intermediate errors, like short connection problems. If too many operations
fail it is safe to assume, that a bigger problem is occurring and that subsequent operations will also fail. In this
case the program is terminated to prevent unnecessary load.

### Retention

The same retention period, as on the database is also enforced on the S3 compatible storage.

## Assembly Process

The exported diagnosis-keys are being organized in hourly archives. The folder structure is as follows:
`/version/<version>/diagnosis-keys/country/<ISO-3166-country>/date/<YYYY-MM-DD>/hour/<hh>/index`. The version, country,
date and hour directory also contain an index file each, listing all the subdirectories.

For each assembly run the diagnosis keys for the last 14 days are queried. Based on the result, hour and their parent
directories are created and the keys are added to their respective archives. To which archive the key should be added is
determined by the distribution timestamp.

The diagnosis keys need to be expired for at least two hours, before they can be distributed. This means that if the key
has been submitted within two hours after the expiry date, it cannot be published immediately. Therefore a distribution
timestamp is calculated, which is either the submission timestamp, or, if the submission timestamp is within two hours
after the expiry date, the expiry date plus two hours. This ensures compliance with the specification from Google and
Apple.

Since all hourly archives are always created for the last 14 days, it is possible, that the exact same archive already
resides within the S3 compatible storage. To prevent the unnecessary re-upload of files the [`cwa-hash`](#cwa-hash)
header has been introduced.
