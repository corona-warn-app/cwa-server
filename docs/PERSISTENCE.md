# CWA-Server Persistence

## Data Retention Policy

We enforce a certain retention policy in the database which means that we don't hold diagnosis keys that are older than 14 days.
In other words that means the `rollingStartIntervalNumber` of a diagnosis key must be greater or equal to the threshold.
The number of days can be configured in
[`application.yaml`](/services/distribution/src/test/resources/application.yaml) by using the property
`retention-days`.
