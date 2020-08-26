# CWA Federation Key Upload Service

This service (running as a cronjob) will deal with the upload of DE keys to the federation gateway. The job will be configured to run periodically throughout the day to enable keys to be shared with the federation gateway and will run independently from the DE CDN distribution job. This job will source keys from a dedicated table populated with keys which are relevant for sharing. Once a key is published to the gateway via an upload batch it would be removed from this table to ensure it is not resent.

## Identification of Keys Applicable for Upload

In order to determine which keys are applicable for upload the following use cases are considered:

- Existing Keys on the CWA Backend Prior to Federation Integration: In this case **NO** existing keys will be shared with the federation gateway. This is due to the fact that these keys are from users which did not have the capability to provide a consent for sharing.
- New Submissions post Federation Integration: This will be determined based on new attributes of the submission payload and these keys replicated for use by the upload service. See details under the [Submission Service](./SUBMISSION.md)

### Key Sharing

Due to the nature of sharing keys separately from the distribution runs it is possible that keys from DE Citizens are initially found on federated countries prior to them being released to the DE CDN. This can occur primarily in the following situations:

- The distribution job is failing to run
- The distribution run where the keys are able to be distributed runs after the upload

## DPP Validations for Publishing

During the upload process it must be ensured that the DPP restrictions which are in place for the normal distribution to the CDN are respected when sending to the federation gateway as much as possible. Therefore, the following validations will be put in place when evaluating keys during the cronjob execution:

- Minimum number of keys required prior to uploading: For distribution this is set via a configuration option found within the distribution service configuration. The similar approach would be leveraged for upload and the values will remain equal. At the time of authoring this it was set to 144 keys minimum.
- Minimum period of time delay post submission of the keys: An expiration period is enforced after the key sets are submitted to the backend prior to them becoming applicable for distribution runs. This is set via configuration found in the distribution service. This same logic will be applied to the keys to send to the federation gateway as well. At the time of authoring this is set to 2 hours.

## Key Normalization

TODO: Not sure if this will occur here or in the submission workflow

## Supporting Data Model

Just put a diagram of the table

## Resilience

As this service is a cronjob running inside of kubernetes it will be ensured by the infrastructure that it is running per the schedule defined. Beyond this the following considerations were made to ensure the keys are sent to the federation gateway:

- Duplicated persistent storage for the keys which are required to be sent to the federation gateway. This makes it clear which keys are needing to be sent and as they are persisted if they fail to post in a run they will be re-processed in a future iteration.
- Retry processing around the Federation Gateway API endpoint invocations will be in place incase an initial request fails

## Security

## Batch Signatures

## Batch Tag Generation

