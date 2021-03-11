INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('expired_batch_tag', '2000-01-01', 'UNPROCESSED','EFGS');
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('retry_batch_tag_successful', CURRENT_DATE - 1, 'ERROR','EFGS');
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('retry_batch_tag_fail', CURRENT_DATE - 1, 'ERROR','EFGS');
