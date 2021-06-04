ALTER TABLE federation_batch_info DROP CONSTRAINT federation_batch_info_pkey;
ALTER TABLE federation_batch_info ADD PRIMARY KEY (batch_tag, source_system);
