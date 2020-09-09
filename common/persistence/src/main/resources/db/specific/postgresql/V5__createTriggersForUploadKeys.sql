-- Triggers below facilitate the replication & deletion of diagnosis keys to the dedicated Federation Upload key table

CREATE OR REPLACE FUNCTION mirror_uploadable_keys()
RETURNS TRIGGER AS $$
BEGIN
    IF ( NEW.CONSENT_TO_FEDERATION = TRUE ) THEN
        INSERT INTO federation_upload_key VALUES (NEW.*);
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;
 
CREATE TRIGGER mirror_uploadable_keys_trigger
    AFTER INSERT ON diagnosis_key
    FOR EACH ROW EXECUTE PROCEDURE mirror_uploadable_keys();
 
CREATE OR REPLACE FUNCTION remove_expired_uploadable_keys()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM federation_upload_key WHERE key_data = OLD.key_data;
    RETURN OLD;
END;
$$
LANGUAGE plpgsql;
 
CREATE TRIGGER remove_expired_uploadable_keys_trigger
    AFTER DELETE ON diagnosis_key
    FOR EACH ROW EXECUTE PROCEDURE remove_expired_uploadable_keys();

    
-- Add the necessary permissions for the diagnosis key replication triggers

GRANT SELECT, DELETE ON TABLE federation_upload_key TO "cwa_distribution";
GRANT INSERT ON TABLE federation_upload_key TO "cwa_submission";