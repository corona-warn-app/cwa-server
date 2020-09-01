# CWA Federation Key Download Service

This service (running as a cronjob) will deal with the download, sematic validation, extraction, and storage of the keys from the federation gateway. The download service leverages the download API of the federation gateway and will recursively trigger downloads of available batches since the last time it executed. Therefore, for the initial release the download service will use the polling mechanism provided by the federation gateway based on `batchTag` and `Date` combinations and it will keep track of its last processed state within the database. When and if the callback service integration is fully realized the polling mechanism would mainly be used for mass loading scenarios.

On the download of keys from the federation gateway a process of normalization needs to take place. This is done to enable the keys to be consumable by the DE CWA app as not all countries support the same approach which is required for the CWA app. The means the following:

- For keys from countries which support the daysSinceOnset scenario will need to be converted into an appropriate transmission risk level
- For keys from countries which support the transmission risk level we might need to translate the value to something which is reasonable for the RKI
- For keys from countries which support neither, or a key doesn't happen to have either value then there will be a need to provide a reasonable default

The rules above would be defined at 2 levels:

- On a per country basis such that the rules can be specified and values derived based on the origin country
- At a global level for situations where we do not have country specific rules

They would also take into consideration other attributes provided within the key data for example report type. The full set of attributes to be evaluated and how is still TBD.

These rules will allow the keys sourced from the federation gateway to be processed within the CWA App and be considered with the risk detection algorithms.

## External Dependencies

- **Vault**: Used for secrets and certificate storage
- **RDBMS**: PostgreSQL as the persistent storage for keys which are downloaded
- **Federation Gateway Service**: The service where the service downloads the keys

## Data Model

This service doesn't specifically introduce any new data model concepts. It will reuse the existing diagnosis key table where it will store the keys that it downloads.

```sql

CREATE TABLE diagnosis_key (
    key_data bytea PRIMARY KEY,
    rolling_period integer NOT NULL,
    rolling_start_interval_number integer NOT NULL,
    submission_timestamp bigint NOT NULL,
    transmission_risk_level integer NOT NULL,
    consent_to_federation boolean NOT NULL DEFAULT FALSE,
    origin_country varchar (2),
    visited_countries varchar (2) [],
    verification_type varchar(20)
);
```