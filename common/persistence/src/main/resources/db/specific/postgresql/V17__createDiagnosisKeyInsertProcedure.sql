CREATE OR REPLACE PROCEDURE insert_diagnosis_key(new_diagnosis_key diagnosis_key)
LANGUAGE plpgsql
AS $$

BEGIN
    IF EXISTS(
        SELECT 1
        FROM public.diagnosis_key existing
        INNER JOIN (
            SELECT key_data, MAX(submission_timestamp) AS submission_timestamp
            FROM public.diagnosis_key
            GROUP BY key_data
        ) latest
        ON existing.key_data = latest.key_data
            AND existing.submission_timestamp = latest.submission_timestamp
        WHERE existing.key_data = new_diagnosis_key.key_data
            AND existing.submission_type = 'SUBMISSION_TYPE_PCR_TEST'
    ) THEN
        RAISE NOTICE 'Ignoring new diagnosis key - latest diagnosis key with this key_data has submission type PCR';
    ELSE
        INSERT INTO public.diagnosis_key VALUES(new_diagnosis_key.*);
    END IF;
END;
$$