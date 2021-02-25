INSERT INTO federation_batch_info(batch_tag, date, status,target_system) VALUES ('expired_batch', '2000-01-01', 'UNPROCESSED','EFGS');
INSERT INTO federation_batch_info(batch_tag, date, status,target_system) VALUES ('valid_batch', CURRENT_DATE, 'UNPROCESSED','EFGS');
INSERT INTO federation_batch_info(batch_tag, date, status,target_system) VALUES ('partially_failing_batch', CURRENT_DATE, 'UNPROCESSED','EFGS');
INSERT INTO federation_batch_info(batch_tag, date, status,target_system) VALUES ('failing_batch', CURRENT_DATE, 'UNPROCESSED','EFGS');
INSERT INTO federation_batch_info(batch_tag, date, status,target_system) VALUES ('batch_with_next_batch', CURRENT_DATE, 'UNPROCESSED','EFGS');
INSERT INTO federation_batch_info(batch_tag, date, status,target_system) VALUES ('retry_batch_successful', CURRENT_DATE, 'ERROR','EFGS');
INSERT INTO federation_batch_info(batch_tag, date, status,target_system) VALUES ('retry_batch_fail', CURRENT_DATE, 'ERROR','EFGS');
