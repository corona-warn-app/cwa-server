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

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;

@DataJdbcTest
class FederationBatchInfoRepositoryTest {

  private static final String batchTag1 = "11111";
  private static final String batchTag2 = "22222";
  private static final LocalDate date1 = LocalDate.parse("2020-08-15");
  private static final LocalDate date2 = LocalDate.parse("2020-08-16");
  private static final String statusError = FederationBatchStatus.ERROR.name();
  private static final String statusProcessed = FederationBatchStatus.PROCESSED.name();
  private static final String statusUnprocessed = FederationBatchStatus.UNPROCESSED.name();
  @Autowired
  private FederationBatchInfoRepository federationBatchInfoRepository;

  @AfterEach
  void tearDown() {
    federationBatchInfoRepository.deleteAll();
  }

  @Test
  void testStatusIsReturnedCorrectly() {
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag1, date1, statusProcessed);
    assertThat(federationBatchInfoRepository.findByStatus(statusProcessed))
        .isEqualTo(singletonList(new FederationBatchInfo(batchTag1, date1, FederationBatchStatus.PROCESSED)));
  }

  @Test
  void testReturnsEmptyIfStatusDoesNotMatch() {
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag1, date1, statusProcessed);
    assertThat(federationBatchInfoRepository.findByStatus(statusUnprocessed)).isEmpty();
  }

  @Test
  void testDoesNothingOnConflict() {
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag1, date1, statusUnprocessed);
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag1, date2, statusError);
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag2, date2, statusError);
    assertThat(federationBatchInfoRepository.findByStatus(statusUnprocessed))
        .isEqualTo(singletonList(new FederationBatchInfo(batchTag1, date1, FederationBatchStatus.UNPROCESSED)));
  }

  @Test
  void testReturnsEmptyListIfNoUnprocessedBatch() {
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag1, date1, statusProcessed);
    assertThat(federationBatchInfoRepository.findByStatus(statusUnprocessed)).isEmpty();
  }
}
