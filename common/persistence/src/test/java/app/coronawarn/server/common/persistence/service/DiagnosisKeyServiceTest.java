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

import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.assertDiagnosisKeysEqual;
import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.buildDiagnosisKeyForDateTime;
import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.buildDiagnosisKeyForSubmissionTimestamp;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.util.Lists.list;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import java.time.OffsetDateTime;
import java.util.Collections;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;

@DataJdbcTest
class DiagnosisKeyServiceTest {

  @Autowired
  private DiagnosisKeyService diagnosisKeyService;

  @Autowired
  private DiagnosisKeyRepository diagnosisKeyRepository;

  @AfterEach
  public void tearDown() {
    diagnosisKeyRepository.deleteAll();
  }

  @Test
  void testRetrievalForEmptyDB() {
    var actKeys = diagnosisKeyService.getDiagnosisKeys();
    assertDiagnosisKeysEqual(Lists.emptyList(), actKeys);
  }

  @Test
  void testSaveAndRetrieve() {
    var expKeys = list(buildDiagnosisKeyForSubmissionTimestamp(0L));

    diagnosisKeyService.saveDiagnosisKeys(expKeys);
    var actKeys = diagnosisKeyService.getDiagnosisKeys();

    assertDiagnosisKeysEqual(expKeys, actKeys);
  }

  @Test
  void testSortedRetrievalResult() {
    var expKeys = list(
        buildDiagnosisKeyForSubmissionTimestamp(1L),
        buildDiagnosisKeyForSubmissionTimestamp(0L));

    diagnosisKeyService.saveDiagnosisKeys(expKeys);

    // reverse to match expected sort order
    Collections.reverse(expKeys);
    var actKeys = diagnosisKeyService.getDiagnosisKeys();

    assertDiagnosisKeysEqual(expKeys, actKeys);
  }

  @DisplayName("Assert a positive retention period is accepted.")
  @ValueSource(ints = {0, 1, Integer.MAX_VALUE})
  @ParameterizedTest
  void testApplyRetentionPolicyForValidNumberOfDays(int daysToRetain) {
    assertThatCode(() -> diagnosisKeyService.applyRetentionPolicy(daysToRetain))
        .doesNotThrowAnyException();
  }

  @DisplayName("Assert a negative retention period is rejected.")
  @ValueSource(ints = {Integer.MIN_VALUE, -1})
  @ParameterizedTest
  void testApplyRetentionPolicyForNegativeNumberOfDays(int daysToRetain) {
    assertThat(catchThrowable(() -> diagnosisKeyService.applyRetentionPolicy(daysToRetain)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void testApplyRetentionPolicyForEmptyDb() {
    diagnosisKeyService.applyRetentionPolicy(1);
    var actKeys = diagnosisKeyService.getDiagnosisKeys();
    assertThat(actKeys).isEmpty();
  }

  @Test
  void testApplyRetentionPolicyForOneNotApplicableEntry() {
    var expKeys = list(buildDiagnosisKeyForDateTime(OffsetDateTime.now(UTC).minusDays(1L)));

    diagnosisKeyService.saveDiagnosisKeys(expKeys);
    diagnosisKeyService.applyRetentionPolicy(1);
    var actKeys = diagnosisKeyService.getDiagnosisKeys();

    assertDiagnosisKeysEqual(expKeys, actKeys);
  }

  @Test
  void testApplyRetentionPolicyForOneApplicableEntry() {
    var keys = list(buildDiagnosisKeyForDateTime(OffsetDateTime.now(UTC).minusDays(1L).minusHours(1)));

    diagnosisKeyService.saveDiagnosisKeys(keys);
    diagnosisKeyService.applyRetentionPolicy(1);
    var actKeys = diagnosisKeyService.getDiagnosisKeys();

    assertThat(actKeys).isEmpty();
  }

  @Test
  void shouldNotUpdateExistingKey() {
    var keyData = "1234567890123456";
    var keys = list(DiagnosisKey.builder()
            .withKeyData(keyData.getBytes())
            .withRollingStartIntervalNumber(600)
            .withTransmissionRiskLevel(2)
            .withCountryCode("DE")
            .withVisitedCountries(Collections.singletonList("DE"))
            .withSubmissionTimestamp(0L)
            .withReportType(ReportType.CONFIRMED_CLINICAL_DIAGNOSIS)
            .build(),
        DiagnosisKey.builder()
            .withKeyData(keyData.getBytes())
            .withRollingStartIntervalNumber(600)
            .withTransmissionRiskLevel(3)
            .withCountryCode("DE")
            .withVisitedCountries(Collections.singletonList("DE"))
            .withSubmissionTimestamp(0L)
            .withReportType(ReportType.CONFIRMED_CLINICAL_DIAGNOSIS)
            .build());

    int actNumberOfInsertedRows = diagnosisKeyService.saveDiagnosisKeys(keys);
    var actKeys = diagnosisKeyService.getDiagnosisKeys();

    assertThat(actNumberOfInsertedRows).isEqualTo(1);
    assertThat(actKeys).hasSize(1);
    assertThat(actKeys.iterator().next().getTransmissionRiskLevel()).isEqualTo(2);
  }

  @Test
  void testReturnedNumberOfInsertedKeysForNoConflict() {
    var keys = list(
        buildDiagnosisKeyForSubmissionTimestamp(1L),
        buildDiagnosisKeyForSubmissionTimestamp(0L));

    int actNumberOfInsertedRows = diagnosisKeyService.saveDiagnosisKeys(keys);

    assertThat(actNumberOfInsertedRows).isEqualTo(2);
  }
}
