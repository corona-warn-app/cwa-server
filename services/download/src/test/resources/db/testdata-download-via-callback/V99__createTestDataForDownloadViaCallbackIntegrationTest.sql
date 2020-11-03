INSERT INTO federation_batch_info(batch_tag, date, status) VALUES ('expired_batch', '2000-01-01', 'UNPROCESSED');
INSERT INTO federation_batch_info(batch_tag, date, status) VALUES ('valid_batch', CURRENT_DATE, 'UNPROCESSED');
INSERT INTO federation_batch_info(batch_tag, date, status) VALUES ('partially_failing_batch', CURRENT_DATE, 'UNPROCESSED');
INSERT INTO federation_batch_info(batch_tag, date, status) VALUES ('failing_batch', CURRENT_DATE, 'UNPROCESSED');
INSERT INTO federation_batch_info(batch_tag, date, status) VALUES ('batch_with_next_batch', CURRENT_DATE, 'UNPROCESSED');
INSERT INTO federation_batch_info(batch_tag, date, status) VALUES ('retry_batch_successful', CURRENT_DATE, 'ERROR');
INSERT INTO federation_batch_info(batch_tag, date, status) VALUES ('retry_batch_fail', CURRENT_DATE, 'ERROR');
