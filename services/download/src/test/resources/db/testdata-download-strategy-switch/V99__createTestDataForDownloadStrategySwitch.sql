-- Two days in the past - Simulate download date based leftovers
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('processed_batch_1', CURRENT_DATE - 2, 'PROCESSED','EFGS') ON CONFLICT DO NOTHING; -- processed result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('processed_batch_2', CURRENT_DATE - 2, 'PROCESSED','EFGS') ON CONFLICT DO NOTHING; -- processed result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('processed_batch_3', CURRENT_DATE - 2, 'PROCESSED','EFGS') ON CONFLICT DO NOTHING; -- processed result

-- One day in the past - Simulate download date based leftovers
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('processed_batch_4', CURRENT_DATE - 1, 'PROCESSED','EFGS') ON CONFLICT DO NOTHING; -- processed result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('processed_batch_5', CURRENT_DATE - 1, 'PROCESSED','EFGS') ON CONFLICT DO NOTHING; -- processed result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('processed_batch_6', CURRENT_DATE - 1, 'PROCESSED','EFGS') ON CONFLICT DO NOTHING; -- processed result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('error_batch_1', CURRENT_DATE - 1, 'ERROR','EFGS') ON CONFLICT DO NOTHING; -- processed result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('error_batch_2', CURRENT_DATE - 1, 'ERROR','EFGS') ON CONFLICT DO NOTHING; -- processed result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('error_batch_3', CURRENT_DATE - 1, 'ERROR','EFGS') ON CONFLICT DO NOTHING; -- processed result

-- SWITCH HAPPENS at this point


