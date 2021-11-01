INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('batchtag', current_date - 2, 'ERROR','CHGS');
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('batchtag1', current_date - 2, 'ERROR','EFGS');

INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('shouldBeDeleted1', current_date, 'ERROR','CHGS');
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('shouldBeDeleted2', current_date, 'ERROR','EFGS');
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('shouldBeDeleted3', current_date, 'ERROR','CHGS');
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('shouldBeDeleted4', current_date, 'ERROR','EFGS');
