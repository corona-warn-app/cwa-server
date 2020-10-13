GRANT UPDATE ON TABLE federation_upload_key TO "cwa_federation_upload";

ALTER TABLE diagnosis_key ALTER COLUMN report_type DROP DEFAULT;
