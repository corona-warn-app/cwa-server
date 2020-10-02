CREATE TABLE federation_batch_info (
    batch_tag varchar(64) PRIMARY KEY,
    date date NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'UNPROCESSED'
);

ALTER TABLE diagnosis_key
    ADD consent_to_federation BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE diagnosis_key
    ADD origin_country VARCHAR(2) DEFAULT 'DE';
ALTER TABLE diagnosis_key
    ADD visited_countries VARCHAR(2)[];
ALTER TABLE diagnosis_key
    ADD report_type VARCHAR(30) DEFAULT 'CONFIRMED_CLINICAL_DIAGNOSIS';
ALTER TABLE diagnosis_key
    ADD days_since_onset_of_symptoms INTEGER;

CREATE TABLE federation_upload_key (
    key_data bytea PRIMARY KEY,
    rolling_period integer NOT NULL,
    rolling_start_interval_number integer NOT NULL,
    submission_timestamp bigint NOT NULL,
    transmission_risk_level integer NOT NULL,
    consent_to_federation boolean NOT NULL DEFAULT FALSE,
    origin_country varchar (2),
    visited_countries varchar (2) [],
    report_type varchar(30),
    days_since_onset_of_symptoms INTEGER,
    batch_tag VARCHAR(50)
);
