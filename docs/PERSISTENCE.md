# CWA-Server Persistence

## Data Migration / Flyway

Corona Warn App Server uses [Flyway](https://flywaydb.org) for database version control. Flyway is an open-source
database migration tool.
The migration scripts are written in plain SQL and are located in the [db](/common/persistence/src/main/resources/db)
directory under `common/persistence/src/main/resources`.
The naming convention of the versioning scripts is `<Prefix><Version>__<Description>.sql`. The default prefix is V.
An appropriate example for a migration script is `V1_createUserTable.sql`.
For more details see
[Flyway SQL-based Migrations](https://flywaydb.org/documentation/concepts/migrations.html#sql-based-migrations).

## Data Retention Policy

In a first step, the diagnosis key distribution application ensures that the database does not store diagnosis keys for
longer than a maximum of 14 days. Therefore, the `RetentionPolicy` runner deletes any diagnosis keys that do not have a
`submissionTimestamp` greater or equal to the respective threshold. Analogously, it removes any files from the object
store which are associated with points in time earlier than 14 days ago.

The number of days can be configured in [`application.yaml`](/services/distribution/src/test/resources/application.yaml)
by using the property `retention-days`.
