DROP VIEW IF EXISTS "submission_stats";

CREATE OR REPLACE VIEW submission_stats AS
	SELECT COUNT(*), TO_TIMESTAMP(submission_timestamp * 3600)::DATE AS submission_date, 'diagnosis_key' AS tab
		FROM diagnosis_key
		GROUP BY submission_date
	UNION 
	SELECT COUNT(*), TO_TIMESTAMP(submission_timestamp * 3600)::DATE AS submission_date, 'check_in_protected_reports'
		FROM check_in_protected_reports
		GROUP BY submission_date
	UNION 
	SELECT COUNT(*), TO_TIMESTAMP(submission_timestamp * 3600)::DATE AS submission_date, 'trace_time_interval_warning'
		FROM trace_time_interval_warning
	GROUP BY submission_date
	UNION 
	SELECT COUNT(*), TO_TIMESTAMP(submission_timestamp * 3600)::DATE AS submission_date, 'federation_upload_key'
		FROM federation_upload_key
	GROUP BY submission_date
	UNION 
	SELECT COUNT(*), TO_TIMESTAMP(submission_timestamp * 3600)::DATE AS submission_date, 'chgs_upload_key'
		FROM chgs_upload_key
	GROUP BY submission_date
	UNION 
	SELECT COUNT(*), submission_date, 'self_report_submissions'
		FROM self_report_submissions
	GROUP BY submission_date
;
