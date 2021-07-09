-- There are three bathes inserted by date based download during switch day: batch_with_next_batch, next_pointer_1, next_pointer_2
-- Additionally, there are inserted batches by date based download for the previous two days.

-- SWITCH HAPPENS at this point

-- callback inform us of same batches as we manually inserted before on date based strategy.
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('batch_with_next_batch', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- ignored result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('next_pointer_1', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- ignored result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('next_pointer_2', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- ignored result

-- below three should be ignored as they are already in ERROR state
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('switch_day_error_1', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- error result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('switch_day_error_2', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- error result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('switch_day_error_3', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- error result

--  new batches coming from callback which were not found earlier by date base.
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('switch_day_new_unprocessed_batch_1', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- processed result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('switch_day_new_unprocessed_batch_2', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- processed result
INSERT INTO federation_batch_info(batch_tag, date, status,source_system) VALUES ('switch_day_new_unprocessed_batch_3', CURRENT_DATE, 'UNPROCESSED','EFGS') ON CONFLICT DO NOTHING; -- processed result

