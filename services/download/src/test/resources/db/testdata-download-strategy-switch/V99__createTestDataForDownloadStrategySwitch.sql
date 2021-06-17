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

-- Switch day - Simulate download date based leftovers
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('switch_day_unprocessed_batch_1', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- processed result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('switch_day_unprocessed_batch_2', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- processed result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('switch_day_unprocessed_batch_3', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- processed result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('switch_day_error_1', CURRENT_DATE, 'ERROR','EFGS') ON CONFLICT DO NOTHING; -- error won't retry result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('switch_day_error_2', CURRENT_DATE, 'ERROR','EFGS') ON CONFLICT DO NOTHING; -- error won't retry result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('switch_day_error_3', CURRENT_DATE, 'ERROR','EFGS') ON CONFLICT DO NOTHING; -- error won't retry result

-- This batch will conflict with the second nextBatchId from 'batch_with_next_batch'.  'batch_with_next_batch' -> 'next_pointer_1' => 'next_pointer_2'
-- Only this one has to be processed
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('next_pointer_2', CURRENT_DATE, 'ERROR','EFGS') ON CONFLICT DO NOTHING; -- processed result

-- SWITCH HAPPENS at this point

-- callback inform us of same batches as we manually inserted before on date based strategy.
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('switch_day_unprocessed_batch_1', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- ignored result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('switch_day_unprocessed_batch_2', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- ignored result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('switch_day_unprocessed_batch_3', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- ignored result

-- below three should be ignored as they are already in ERROR state
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('switch_day_error_1', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- ignored result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('switch_day_error_2', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- ignored result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('switch_day_error_3', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- ignored result

--  new batches coming from callback which were not found earlier by date base.
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('switch_day_new_unprocessed_batch_1', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- processed result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('switch_day_new_unprocessed_batch_2', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- processed result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('switch_day_new_unprocessed_batch_3', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- processed result


INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('batch_with_next_batch', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- processed result

--  new batches coming from callback which were not found earlier by date base.
