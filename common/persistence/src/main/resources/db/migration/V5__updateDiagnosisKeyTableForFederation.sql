-- Migrate current values -> origin_country = DE

ALTER TABLE diagnosis_key
    ADD consent_to_federation BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE diagnosis_key
    ADD origin_country VARCHAR(2) DEFAULT 'DE';

ALTER TABLE diagnosis_key
    ADD visited_countries VARCHAR(2)[];

ALTER TABLE diagnosis_key
    ADD report_type VARCHAR(30) DEFAULT 'CONFIRMED_CLINICAL_DIAGNOSIS';

ALTER TABLE diagnosis_key
    ADD days_since_onset_of_symptoms INTEGER;
