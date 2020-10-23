INSERT INTO federation_batch_info(batch_tag, date, status) VALUES ('expired_batch_tag', '2000-01-01', 'UNPROCESSED');
INSERT INTO federation_batch_info(batch_tag, date, status) VALUES ('valid_tag', CURRENT_DATE, 'UNPROCESSED');
INSERT INTO federation_batch_info(batch_tag, date, status) VALUES ('processed_with_error', CURRENT_DATE, 'UNPROCESSED');
INSERT INTO federation_batch_info(batch_tag, date, status) VALUES ('processing_fails', CURRENT_DATE, 'UNPROCESSED');
INSERT INTO federation_batch_info(batch_tag, date, status) VALUES ('retry_batch_tag_successful', CURRENT_DATE, 'ERROR');
INSERT INTO federation_batch_info(batch_tag, date, status) VALUES ('retry_batch_tag_fail', CURRENT_DATE, 'ERROR');
