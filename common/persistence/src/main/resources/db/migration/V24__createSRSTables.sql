CREATE TABLE IF NOT EXISTS self_report_submissions (
    submission_type VARCHAR(40) NOT NULL,
    submission_date DATE NOT NULL DEFAULT CURRENT_DATE
);

GRANT INSERT ON TABLE self_report_submissions TO "cwa_submission";

CREATE OR REPLACE VIEW self_reports AS
	SELECT COUNT(*), submission_date, submission_type
		FROM self_report_submissions
	GROUP BY submission_date, submission_type
;

ALTER TABLE diagnosis_key ALTER COLUMN submission_type TYPE VARCHAR(40);
ALTER TABLE federation_upload_key ALTER COLUMN submission_type TYPE VARCHAR(40);
ALTER TABLE chgs_upload_key ALTER COLUMN submission_type TYPE VARCHAR(40);
ALTER TABLE trace_time_interval_warning ALTER COLUMN submission_type TYPE VARCHAR(40);
