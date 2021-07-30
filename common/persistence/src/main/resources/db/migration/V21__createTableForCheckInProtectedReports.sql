CREATE TABLE check_in_protected_reports (
  id SERIAL PRIMARY KEY,
  trace_location_ID_hash BYTEA NOT NULL,
  start_interval_number INTEGER NOT NULL,
  period INTEGER NOT NULL,
  transmission_risk_level int,
  submission_timestamp BIGINT NOT NULL
);
