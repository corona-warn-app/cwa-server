INSERT INTO federation_batch_info(batch_tag, date, status) VALUES ('expired_batch_tag', '2000-01-01', 'UNPROCESSED');
INSERT INTO federation_batch_info(batch_tag, date, status) VALUES ('retry_batch_tag_successful', CURRENT_DATE - 1, 'ERROR');
INSERT INTO federation_batch_info(batch_tag, date, status) VALUES ('retry_batch_tag_fail', CURRENT_DATE - 1, 'ERROR');
