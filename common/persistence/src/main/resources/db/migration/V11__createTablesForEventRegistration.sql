CREATE TABLE trace_time_interval_warning (
  id SERIAL PRIMARY KEY,
  trace_location_id BYTEA NOT NULL,
  start_interval_number INTEGER NOT NULL,
  period INTEGER NOT NULL,
  transmission_risk_level INTEGER NOT NULL,
  submission_timestamp BIGINT NOT NULL
);
