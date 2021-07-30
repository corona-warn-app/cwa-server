CREATE TABLE check_in_protected_reports (
  id SERIAL PRIMARY KEY,
  trace_location_ID_hash BYTEA NOT NULL,
  initialization_vector BYTEA NOT NULL,
  encrypted_check_in_record BYTEA NOT NULL,
  submission_timestamp BIGINT NOT NULL
);
