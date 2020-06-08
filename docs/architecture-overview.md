# Architecture CWA Server

This document outlines the CWA server architecture on high level. This document
does not necessarily reflect the current implementation status in this repository, as development
is still ongoing. Also, details of the exposure notification API by Google/Apple might change
in the future.

Please note: This overview document only focuses on the architecture of this component.
If you are interested in the full architectural overview, check out the [solution architecture](https://github.com/corona-warn-app/cwa-documentation/blob/master/solution_architecture.md)
in the [cwa-documentation](https://github.com/corona-warn-app/cwa-documentation) repository.

## Overview

The Corona Warn App ("CWA") server runs in a Kubernetes cluster on the Open Shift (“OSP”) platform.
Its main objective is to enable users to take part in the exposure notification framework based
on Apple/Google specifications. Although CWA aims at compliance to the spec on a protocol level, it
does not mean automatically that all features will be implemented. Main driver for these assessments is data privacy and protection (DPP) concerns.

Find the latest specifications of Google/Apple here:

- [Exposure Key Export File Format and Verification](https://static.googleusercontent.com/media/www.google.com/en//covid19/exposurenotifications/pdfs/Exposure-Key-File-Format-and-Verification.pdf)
- [Setting Up an Exposure Notification Server (Apple)](https://developer.apple.com/documentation/exposurenotification/setting_up_an_exposure_notification_server?changes=latest_beta)
- [Apple Framework Specifications](https://developer.apple.com/documentation/exposurenotification?changes=latest)
- [Google Framework Specifications (1.3.2)](https://static.googleusercontent.com/media/www.google.com/en//covid19/exposurenotifications/pdfs/Android-Exposure-Notification-API-documentation-v1.3.2.pdf)
On a high level, the application consists of two main parts, as shown below.

![Overview Diagram](./images/v4.png)

1. CWA Server: Handles submission and aggregation/distribution of diagnosis keys and configuration files.
2. Verification Server: Deals with test result retrieval and verification (including issuing TANs).
The components regarding the verification are managed and deployed separately.

This document outlines the CWA Server components, which are part of this repository. For the full architectural
overview, check out the [solution architecture](https://github.com/corona-warn-app/cwa-documentation/blob/master/solution_architecture.md).

## Integration with Other Systems

### Object Store

All mobile app relevant files will be stored on the S3 Object Store. Those files are:

- Aggregated files containing the diagnosis keys, which were reported in a specific interval (e.g. hourly).
- Daily aggregated files containing the diagnosis keys, which were reported for the respective days.
- Configuration files containing the [exposure configuration](https://developer.apple.com/documentation/exposurenotification/enexposureconfiguration) and CWA mobile app config.
- Additional files regarding meta information of available files/structures/etc.

The files will be pushed to an S3 compliant object store whenever new files become available. File structure definitions for those files can be found in the respective Services chapter.

The mobile application will use a CDN for fetching files, which mirrors all files as a transparent proxy present in the object store.

### Verification Server

The verification server supports the user's journey beginning at scanning the QR code printed
on the documentation of the SARS-CoV-2 test until the upload of diagnosis keys when the user was tested positive. Testing
labs will provide the results of SARS-CoV-2 tests to the [test result server](https://github.com/corona-warn-app/cwa-testresult-server),
which in turn provides an interface to the [verification server](https://github.com/corona-warn-app/cwa-verification-server).
Since the GUID contained in the QR code is linked to a test, the mobile application is able to fetch the results from
the verification server and provide a notification to the user. After users have given their consent to upload their diagnosis key,
a TAN is fetched from the verification server. This TAN will be used as an authorization token when
the user uploads the diagnosis keys of the past 14 days.

Therefore, from a CWA Server perspective, the Verification Server provides an endpoint for TAN verification.

A detailed description of the process can be found in the chapter ["Retrieval of lab results and verification process"](https://github.com/corona-warn-app/cwa-documentation/blob/master/solution_architecture.md#retrieval-of-lab-results-and-verification-process) of the solution architecture document.

## Security

### Endpoint Protection

The CWA Server exposes only one endpoint – the submission endpoint.
The endpoint is public (unauthenticated), and authorization for calls is granted to users who are passing a valid TAN.
The TAN verification cannot be done on CWA Server, but the task is delegated to the verification server (see Verification Server chapter in *Integration with other Systems*).

### Authenticity

All files published by CWA will be digitally signed by CWA.
This ensures that clients can trust the file they have received from the third-party CDN.

The [protocol buffer](https://developers.google.com/protocol-buffers) files from Google/Apple already specify how signing should work. Each diagnosis key aggregate file
is a zip file, containing two files - one carries the actual payload. The other one carries signature information.

CWA is be required to share the public key with Google/Apple, so that the API on the mobile client
is able verify against it.

### Fake Submissions (Plausible Deniability)

In order to protect the privacy of the users, the mobile app needs to send dummy submissions from time to time.
The server accepts the incoming calls and treats them the same way as regular submissions.
The payload and behavior of dummy and real requests must be similar, so that 3rd parties are unable to differentiate between those requests.
If a submission request marked as a dummy is received by the server, the caller will be presented with a successful result.
The CWA server does not persist the entry on the database and ensures that dummy and real requests take the same amount of total response time, e.g. by delaying the response to the client, if necessary.

## Services

### Submission Service

The submission service's only task is to process uploaded diagnosis keys and persist them to the database after the TAN has been verified.
The actual task of the verification is handed over to the verification server, which provides the verification result back to CWA.
After verification was successfully done, the diagnosis keys are persisted in the database, and will be published in the next batch.

The payload to be sent by the mobile applications is defined as:

```protobuf
message SubmissionPayload {
  repeated Key keys = 1;
}

message Key {
  bytes keyData = 1; // Key of infected user
  uint32 rollingStartIntervalNumber = 2; // Interval number when the key's EKRollingPeriod started.
  uint32 rollingPeriod = 3; // Number of 10-minute windows between key rolling.
  int32 transmissionRiskLevel = 4; // Risk of transmission associated with the person this key came from.
}
```

Additionally, the endpoint requires the following headers to be set:

```http
CWA-Authorization: TAN <TAN>
CWA-Fake: <0 or 1>
```

There is currently no official specification for publishing diagnosis keys to the server.
Google currently uses the following in their reference implementation.

[exposure_types.go](https://github.com/google/exposure-notifications-server/blob/master/pkg/api/v1alpha1/exposure_types.go)

```golang
type Publish struct {
  Keys                []ExposureKey `json:"temporaryExposureKeys"`
  Regions             []string      `json:"regions"`
  AppPackageName      string        `json:"appPackageName"`
  VerificationPayload string        `json:"verificationPayload"`
  HMACKey             string        `json:"hmackey"`
  Padding             string        `json:"padding"`

  Platform                  string `json:"platform"`                  // DEPRECATED
  DeviceVerificationPayload string `json:"deviceVerificationPayload"` // DEPRECATED
}
```

Due to concerns regarding data privacy and protection, device attestation is currently not being used by CWA.

### Distribution Service

The distribution service's objective is to publish all CWA-related files to the object store, from which
the clients will fetch their data. There are three types of files.

#### Key Export

Key Export files are files, which hold published diagnosis keys from users that have tested positive for SARS-CoV-2.
These files are based on the specification of Google/Apple and are generated in regular intervals.
Each interval generates a `.zip` file, containing two files:

1. export.bin: Contains the list of diagnosis keys.
2. export.sig: Contains signature information needed for validating the export.bin file.
The file structure definition can be found [here](https://github.com/google/exposure-notifications-server/blob/master/internal/pb/export/export.proto).

The distribution service is triggered by a CRON scheduler, currently set to 1 hour. However, this
will change, since the Exposure Notification APIs have a rate-limiting in place (Apple: 15 files/day, Google 20 API calls/day).

In case there is an insufficient number of diagnosis keys for the target interval available, the
creation of the file will be skipped in order to prevent attackers from being potentially able to
de-obfuscate individuals.

Another alternative is to put fake diagnosis keys into the payload, which would serve the same purpose.
In that case, it needs to be guaranteed, that those fake diagnosis keys are indistinguishable from real ones.

#### Configuration

Configuration files are needed for two use cases:

1. Exposure Configuration: In order to calculate a risk score for each exposure incident, the mobile
API requires a list of the following parameters, requiring weights and levels: duration, days, attenuation and transmission.
The function and impact of those parameters is described on the [Apple Exposure Configuration Page](https://developer.apple.com/documentation/exposurenotification/enexposureconfiguration) and in the chapter [*Risk score calculation*](https://github.com/corona-warn-app/cwa-documentation/blob/master/solution_architecture.md#risk-score-calculation) of the solution architecture document.
2. Mobile App Configuration: Provides configuration values needed for the CWA mobile app, which are
not part of the exposure notification framework. These values are required for controlling the
application behavior.

#### Discovery

Files on CWA may be discovered with the help of index files. There is one central index file,
containing all available key export files on the server, separated by new-line.
The index will be regenerated whenever new export files are distributed to the object store.

## Data Retention

The retention period is set to 14 days. Therefore, all keys whose _submission date_ is older than 14 days are removed from the system. This includes the database persistency layer, as well as files stored on the object store.
The retention mechanism is enforced automatically by the Distribution Service upon each distribution run (multiple runs per day).
No manual trigger or action is required.

Data is deleted by normal means. For PostgreSQL, the identified rows will be deleted by normal __DELETE__ calls to the database, and
cleaned up when auto vacuuming is executed.
When data deletion is executed on the object store, the object store is instructed to delete all
files with the following prefix:

`version/v1/diagnosis-keys/country/DE/<date>`

In which `<date>` stands for the ISO formatted date (e.g. `2012-06-05`), and is before the retention cutoff date (today - 14 days).
