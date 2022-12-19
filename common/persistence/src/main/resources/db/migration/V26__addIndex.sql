/* Submission Controller - diagnosisKeyService.exists(...) */
CREATE INDEX IF NOT EXISTS key_data_idx ON diagnosis_key (key_data);

/* DiagnosisKeysStructureProvider - diagnosisKeyService.getDiagnosisKeysWithMinTrl(...) */
CREATE INDEX IF NOT EXISTS trl_and_time_idx ON diagnosis_key (transmission_risk_level, submission_timestamp);

/* Distribution */
CREATE INDEX IF NOT EXISTS submission_time_idx ON diagnosis_key (submission_timestamp);
