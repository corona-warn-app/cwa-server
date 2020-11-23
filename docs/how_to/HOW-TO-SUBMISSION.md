# Submission Tutorial

## Sending a Submission Payload with Postman

* Ensure that the following docker container are running:
  1. `cwa-server_postgres`
  2. `cwa-server_verification-fake`
* Start the submission service via your IDE (alternatively, start the corresponding docker container).
* Prepare a new POST-request to URL: `http://localhost:8080/version/v1/diagnosis-keys`.
* Set headers as shown below.
* Attach a valid submission payload binary file as request body (you can use the SubmissionPayloadGenerator as explained below).
* Send request. The expected response has an empty body and status `200`.

## Request Header Settings in Postman

Key                       | Value
--------------------------|-------------
CWA-Authorization         | `<tan>` (Valid Tan: `edc07f08-a1aa-11ea-bb37-0242ac130002`)
CWA-Fake                  | `0` (Set to `1` to test fake-request handling)
Content-Type              | `application/x-protobuf`

## Using the built-in SubmissionPayLoadGenerator

* Checkout the cwa-server repository.
* Run `app.coronawarn.server.services.submission.SubmissionPayloadGenerator` as an application.
* The payload can be found here: `services/submission/src/test/resources/payload/mobile-client-payload.pb`.

## Using the external SubmissionPayloadGenerator

* Checkout the cwa-server-tools repository
* Run `app.coronawarn.server.tools.SubmissionPayloadGenerator` as an application.
* The payload `5keys.bin` can be found in the projects root directory.
