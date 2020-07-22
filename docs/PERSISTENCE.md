# CWA-Server Persistence

## Data Migration / Flyway

Coming soon

## Data Retention Policy

In a first step, the diagnosis key distribution application ensures that the database does not store diagnosis keys for
longer than a maximum of 14 days. Therefore, the `RetentionPolicy` runner deletes any diagnosis keys that do not have a
`submissionTimestamp` greater or equal to the respective threshold. Analogously, it removes any files from the object
store which are associated with points in time earlier than 14 days ago.

The number of days can be configured in [`application.yaml`](/services/distribution/src/test/resources/application.yaml)
by using the property `retention-days`.
