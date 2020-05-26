ALTER TABLE diagnosis_key ALTER COLUMN rolling_period TYPE integer;
ALTER TABLE diagnosis_key ALTER COLUMN rolling_start_number TYPE integer;
ALTER TABLE diagnosis_key RENAME COLUMN rolling_start_number TO rolling_start_interval_number;
