-- Migrate current values -> origin_country = DE

ALTER TABLE diagnosis_key
    ADD origin_country CHAR(2);

ALTER TABLE diagnosis_key
    ADD visited_countries ARRAY;
