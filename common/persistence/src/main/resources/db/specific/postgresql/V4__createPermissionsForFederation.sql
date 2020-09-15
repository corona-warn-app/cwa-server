GRANT SELECT, INSERT ON TABLE federation_batch_info TO "cwa_federation_callback";

GRANT ALL ON TABLE federation_batch_info TO "cwa_federation_download";
GRANT INSERT ON TABLE diagnosis_key TO "cwa_federation_download";

GRANT SELECT, DELETE ON TABLE federation_upload_key TO "cwa_federation_upload";
