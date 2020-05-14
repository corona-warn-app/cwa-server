CREATE TABLE diagnosis_key (
    id bigserial PRIMARY KEY,
    key_data bytea,
    rolling_period bigint NOT NULL,
    rolling_start_number bigint NOT NULL,
    submission_timestamp bigint NOT NULL,
    transmission_risk_level integer NOT NULL
);