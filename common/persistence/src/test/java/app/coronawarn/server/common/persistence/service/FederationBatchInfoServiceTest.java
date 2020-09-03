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

package app.coronawarn.server.common.persistence.service;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.repository.FederationBatchInfoRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;

@DataJdbcTest
class FederationBatchInfoServiceTest {

  LocalDate date = LocalDate.of(2020, 9, 1);
  String batchTag1 = "91e810c19729de860ea";
  String batchTag2 = "91e810c19729de860eb";

  @Autowired
  private FederationBatchInfoService federationBatchInfoService;

  @Autowired
  private FederationBatchInfoRepository federationBatchInfoRepository;

  @AfterEach
  public void tearDown() {
    federationBatchInfoRepository.deleteAll();
  }

  @Test
  void testRetrievalForEmptyDb() {
    var actKeys = federationBatchInfoService.findByStatus(FederationBatchStatus.UNPROCESSED);
    assertThat(actKeys).isEmpty();
  }

  @Test
  void testSaveAndRetrieve() {
    FederationBatchInfo federationBatchInfo = new FederationBatchInfo(batchTag1, date);
    federationBatchInfoService.save(federationBatchInfo);
    var actualKeys = federationBatchInfoService.findByStatus(FederationBatchStatus.UNPROCESSED);
    assertThat(actualKeys.size()).isEqualTo(1);
    assertThat(actualKeys.get(0)).isEqualTo(federationBatchInfo);
  }

  @Test
  void testSaveAndRetrieveDifferentStatus() {
    FederationBatchInfo federationBatchInfo = new FederationBatchInfo(batchTag1, date);
    federationBatchInfoService.save(federationBatchInfo);
    var actualKeys = federationBatchInfoService.findByStatus(FederationBatchStatus.ERROR);
    assertThat(actualKeys).isEmpty();
  }

  @Test
  void testUpdateStatus() {
    FederationBatchInfo federationBatchInfo = new FederationBatchInfo(batchTag1, date, FederationBatchStatus.UNPROCESSED);
    federationBatchInfoService.updateStatus(federationBatchInfo,FederationBatchStatus.PROCESSED);

    var actualUnprocessedKeys = federationBatchInfoService.findByStatus(FederationBatchStatus.UNPROCESSED);
    assertThat(actualUnprocessedKeys).isEmpty();

    var actualProcessedKeys = federationBatchInfoService.findByStatus(FederationBatchStatus.PROCESSED);
    assertThat(actualProcessedKeys.size()).isEqualTo(1);
    assertThat(actualProcessedKeys.get(0)).isEqualTo(new FederationBatchInfo(batchTag1, date, FederationBatchStatus.PROCESSED));
  }

  @Test
  void testSaveAndRetrieveOnConflict() {
    FederationBatchInfo federationBatchInfo1 = new FederationBatchInfo(batchTag1, date, FederationBatchStatus.UNPROCESSED);
    federationBatchInfoService.save(federationBatchInfo1);

    FederationBatchInfo federationBatchInfo2 = new FederationBatchInfo(batchTag1, date, FederationBatchStatus.ERROR);
    federationBatchInfoService.save(federationBatchInfo2);

    var actualErrorKeys = federationBatchInfoService.findByStatus(FederationBatchStatus.ERROR);
    assertThat(actualErrorKeys).isEmpty();

    var actualKeys = federationBatchInfoService.findByStatus(FederationBatchStatus.UNPROCESSED);
    assertThat(actualKeys.size()).isEqualTo(1);
    assertThat(actualKeys.get(0)).isEqualTo(federationBatchInfo1);
  }

}
