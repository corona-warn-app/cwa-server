/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.FederationBatch;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FederationBatchRepository extends PagingAndSortingRepository<FederationBatch, String> {

  @Modifying
  @Query("INSERT INTO federation_batch "
      + "(batch_tag, date, status) "
      + "VALUES (:batchTag, :date, :status) "
      + "ON CONFLICT DO NOTHING")
  void saveDoNothingOnConflict(
      @Param("batchTag") String batchTag,
      @Param("date") LocalDate date,
      @Param("status") FederationBatchStatus status
  );

  @Query("SELECT * FROM federation_batch WHERE status != 'PROCESSED' OR status is null ORDER BY date asc LIMIT 1")
  FederationBatch findOldestUnprocessedFederationBatch();

  @Query("SELECT * FROM federation_batch WHERE status = :status")
  List<FederationBatch> findByStatus(@Param("status") String status);
}
