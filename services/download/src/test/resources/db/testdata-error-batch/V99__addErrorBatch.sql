INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('batchtag', '2021-06-01', 'ERROR','CHGS');
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('batchtag1', '2021-06-01', 'ERROR','EFGS');

INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('shouldBeDeleted1', current_date, 'ERROR','CHGS');
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('shouldBeDeleted2', current_date, 'ERROR','EFGS');
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('shouldBeDeleted3', current_date, 'ERROR','CHGS');
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('shouldBeDeleted4', current_date, 'ERROR','EFGS');
