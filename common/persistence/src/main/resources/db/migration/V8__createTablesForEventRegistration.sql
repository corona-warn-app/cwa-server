CREATE TABLE trace_time_interval_warning (
  id SERIAL PRIMARY KEY,
  trace_location_guid BYTEA NOT NULL,
  start_interval_number INTEGER NOT NULL,
  end_interval_number INTEGER NOT NULL,
  transmission_risk_level INTEGER NOT NULL
);
