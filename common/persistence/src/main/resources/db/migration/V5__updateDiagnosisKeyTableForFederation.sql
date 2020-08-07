-- Migrate current values -> origin_country = DE

ALTER TABLE diagnosis_key
    ADD origin_country VARCHAR (2);

ALTER TABLE diagnosis_key
    ADD visited_countries VARCHAR (2) [];

ALTER TABLE diagnosis_key
    ADD verification_type VARCHAR(20);
