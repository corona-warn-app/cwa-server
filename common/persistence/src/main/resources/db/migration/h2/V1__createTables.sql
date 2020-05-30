CREATE TABLE diagnosis_key (
    key_data bytea PRIMARY KEY,
    rolling_period bigint NOT NULL,
    rolling_start_number bigint NOT NULL,
    submission_timestamp bigint NOT NULL,
    transmission_risk_level integer NOT NULL
);