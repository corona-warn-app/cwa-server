ALTER TABLE diagnosis_key DROP COLUMN id;
ALTER TABLE diagnosis_key ADD PRIMARY KEY (key_data);
