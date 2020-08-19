CREATE TABLE federation_upload_key (
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
