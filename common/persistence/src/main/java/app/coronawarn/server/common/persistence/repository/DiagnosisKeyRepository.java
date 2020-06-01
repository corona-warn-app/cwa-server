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

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnosisKeyRepository extends JpaRepository<DiagnosisKey, Long> {

  /**
   * Deletes all entries that have a submission timestamp lesser or equal to the specified one.
   *
   * @param submissionTimestamp the submission timestamp up to which entries will be deleted.
   */
  void deleteBySubmissionTimestampIsLessThanEqual(long submissionTimestamp);
}
