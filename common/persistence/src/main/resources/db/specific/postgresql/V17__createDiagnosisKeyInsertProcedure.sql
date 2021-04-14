CREATE OR REPLACE PROCEDURE insert_diagnosis_key(new_key_data bytea, rolling_start_interval_number integer, rolling_period integer, submission_timestamp bigint, transmission_risk_level integer, origin_country varchar(2), visited_countries varchar(2)[], report_type varchar(30), days_since_onset_of_symptoms integer, consent_to_federation boolean, submission_type varchar(30))
LANGUAGE plpgsql
AS $$

BEGIN
    IF EXISTS(
        SELECT 1
        FROM public.diagnosis_key existing
        WHERE existing.key_data = new_key_data
            AND existing.submission_type = 'SUBMISSION_TYPE_PCR_TEST'
    ) THEN
        RAISE NOTICE 'Ignoring new diagnosis key - a diagnosis key with this key_data and submission type PCR already exists.';
    ELSE
        INSERT INTO public.diagnosis_key (key_data, rolling_start_interval_number, rolling_period, submission_timestamp, transmission_risk_level, origin_country, visited_countries, report_type, days_since_onset_of_symptoms, consent_to_federation, submission_type)
            VALUES(new_key_data, rolling_start_interval_number, rolling_period, submission_timestamp, transmission_risk_level, origin_country, visited_countries, report_type, days_since_onset_of_symptoms, consent_to_federation, submission_type)
            ON CONFLICT DO NOTHING;
    END IF;
END;
$$
