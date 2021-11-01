CREATE TABLE check_in_protected_reports (
    id SERIAL PRIMARY KEY,
    trace_location_id_hash BYTEA NOT NULL,
    initialization_vector BYTEA NOT NULL,
    encrypted_check_in_record BYTEA NOT NULL,
    submission_timestamp BIGINT NOT NULL
);

GRANT INSERT ON TABLE check_in_protected_reports TO "cwa_submission";
GRANT USAGE, SELECT ON SEQUENCE check_in_protected_reports_id_seq TO "cwa_submission";

GRANT SELECT, DELETE ON TABLE check_in_protected_reports TO "cwa_distribution";
GRANT USAGE, SELECT ON SEQUENCE check_in_protected_reports_id_seq TO "cwa_distribution";
