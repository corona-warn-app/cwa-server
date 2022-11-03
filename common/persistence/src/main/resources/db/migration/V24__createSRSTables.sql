CREATE TABLE IF NOT EXISTS self_report_submissions (
    submission_type VARCHAR(30) NOT NULL,
    submission_date DATE NOT NULL DEFAULT CURRENT_DATE
);

GRANT INSERT ON TABLE self_report_submissions TO "cwa_submission";

CREATE OR REPLACE VIEW self_reports AS
	SELECT COUNT(*), submission_date, submission_type
		FROM self_report_submissions
	GROUP BY submission_date, submission_type
;
