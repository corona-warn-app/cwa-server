ALTER TABLE diagnosis_key DROP CONSTRAINT diagnosis_key_pkey;
ALTER TABLE diagnosis_key ADD COLUMN submission_type VARCHAR(30) DEFAULT 'SUBMISSION_TYPE_PCR_TEST';
ALTER TABLE diagnosis_key ALTER COLUMN submission_type DROP DEFAULT;
ALTER TABLE diagnosis_key ALTER COLUMN submission_type SET NOT NULL;
ALTER TABLE diagnosis_key ADD PRIMARY KEY (key_data, submission_type);

ALTER TABLE federation_upload_key ADD COLUMN submission_type VARCHAR(30) DEFAULT 'SUBMISSION_TYPE_PCR_TEST';
ALTER TABLE federation_upload_key ALTER COLUMN submission_type DROP DEFAULT;
ALTER TABLE federation_upload_key ALTER COLUMN submission_type SET NOT NULL;

ALTER TABLE chgs_upload_key ADD COLUMN submission_type VARCHAR(30) DEFAULT 'SUBMISSION_TYPE_PCR_TEST';
ALTER TABLE chgs_upload_key ALTER COLUMN submission_type DROP DEFAULT;
ALTER TABLE chgs_upload_key ALTER COLUMN submission_type SET NOT NULL;

ALTER TABLE trace_time_interval_warning ADD COLUMN submission_type VARCHAR(30) DEFAULT 'SUBMISSION_TYPE_PCR_TEST';
ALTER TABLE trace_time_interval_warning ALTER COLUMN submission_type DROP DEFAULT;
ALTER TABLE trace_time_interval_warning ALTER COLUMN submission_type SET NOT NULL;