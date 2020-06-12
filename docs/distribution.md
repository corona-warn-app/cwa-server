# CWA-Server Distribution Server

## Configuration Properties

All of the configurable parameters, that are used throught this service can be found in the `application.yaml` file. This configuration file is divided into different sub-categories:

- Some general configuration, which isn't divided into sub-categories
- `paths` - Local Paths, that are used during the export creation
- `tek-export` - Configuration for the exported archive, that is saved on the S3-compatibale storage
- `api` - API Configuration, configures the API, which is used by the mobile app to query diagnosis keys
- `signature` - Signature Configuration, used for signing the exports
- `objectstore` - Configuration for the S3 compatible object storage

## Object Store

The communication with the S3 compatible object storage, that is hosted by Deutsche Telekom is done through the AWS SDK v2. The files, that will be updated to the S3 compatible storage are created on local storage first (ouput path defined in [`application.yaml`](https://github.com/corona-warn-app/cwa-server/blob/master/services/distribution/src/main/resources/application.yaml#L20)) and are then uploaded to the storage provider.

### Headers

Up to three headers are set during upload, depending on the configuration properties.

#### `Cache-Control`

Defines the maximum amount of time a published resource is considered fresh, when held in cache. Will be set for each upload, default is 300 seconds.

#### `x-amz-acl`

Defines the canned ACL for the uploaded file. Is only set if [`set-public-read-acl-on-put-object`](https://github.com/corona-warn-app/cwa-server/blob/master/services/distribution/src/main/resources/application.yaml#L50) is set to true in the configuration properties. Will be `public-read` in that case, which grants full controll to the Owners and read-access to AllUsers.

#### `cwa-hash`

Custom header, which adds the sha256 hash of the archive contents. This header is used to determine, whether a file should be uploaded or not. If the hash for the file is the same as the hash available on the S3 compatible storage it will not be uploaded, since the contents of that specific file did not change, so there is no need to reupload the file. If the hash differs, or the file is not available on the S3 compatible storaget the file will be uploaded.

This header is needed, since it is not possible to create byte-identical archives, due to the signing alogrithms.

### Threading

The upload operations are being run in multiple threads to increase performance. The number of threads is defined in the [`application.yaml`](https://github.com/corona-warn-app/cwa-server/blob/master/services/distribution/src/main/resources/application.yaml#L54). SpringBoot's `ThreadPoolTaskExecutor` is used to create and manage the threads. Each upload operation will be submitted as a single task, which then will be distributed across the available threads. Once all threads are submitted, the logic checks, whether all threads are finished before shutting down the Thread Pool. If errors are thrown, there are handled like explained in the next section.

### Error Handling

To make the distribution service as resilient as possible two error handling measures were have been introduced.

The first one being SpringBoot's Retry logic, which is applied to all S3 operations with the number of retries being specified in the ['application.yaml'](https://github.com/corona-warn-app/cwa-server/blob/master/services/distribution/src/main/resources/application.yaml#L51). This results in SpringBoot retrying the operation up to three times, with a two second pause in between.
