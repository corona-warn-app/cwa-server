CREATE TABLE trace_location (
  id SERIAl PRIMARY KEY,
  trace_location_guid_hash BYTEA UNIQUE NOT NULL,
  version INTEGER NOT NULL,
  created_at BIGINT NOT NULL
);

CREATE TABLE trace_time_interval_warnings (
  id SERIAL PRIMARY KEY,
  trace_location_guid BYTEA NOT NULL,
  start_interval_number INTEGER NOT NULL,
  end_interval_number INTEGER NOT NULL,
  transmission_risk_level INTEGER NOT NULL
);
