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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
    var expKeys = List.of(buildDiagnosisKeyForSubmissionTimestamp(0L));

    diagnosisKeyService.saveDiagnosisKeys(expKeys);
    var actKeys = diagnosisKeyService.getDiagnosisKeys();

    assertDiagnosisKeysEqual(expKeys, actKeys);
  }

  @Test
  void testSortedRetrievalResult() {
    var expKeys = new ArrayList<>(List.of(
        buildDiagnosisKeyForSubmissionTimestamp(1L),
        buildDiagnosisKeyForSubmissionTimestamp(0L)));

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
    assertThatCode(() -> diagnosisKeyService.applyRetentionPolicy(daysToRetain, "DE"))
        .doesNotThrowAnyException();
  }

  @DisplayName("Assert a negative retention period is rejected.")
  @ValueSource(ints = {Integer.MIN_VALUE, -1})
  @ParameterizedTest
  void testApplyRetentionPolicyForNegativeNumberOfDays(int daysToRetain) {
    assertThat(catchThrowable(() -> diagnosisKeyService.applyRetentionPolicy(daysToRetain, "DE")))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void testApplyRetentionPolicyForEmptyDb() {
    diagnosisKeyService.applyRetentionPolicy(1, "DE");
    var actKeys = diagnosisKeyService.getDiagnosisKeys();
    assertDiagnosisKeysEqual(Lists.emptyList(), actKeys);
  }

  @Test
  void testApplyRetentionPolicyForOneNotApplicableEntry() {
    var expKeys = List.of(buildDiagnosisKeyForDateTime(OffsetDateTime.now(UTC).minusHours(23)));

    diagnosisKeyService.saveDiagnosisKeys(expKeys);
    diagnosisKeyService.applyRetentionPolicy(1, "DE");
    var actKeys = diagnosisKeyService.getDiagnosisKeys();

    assertDiagnosisKeysEqual(expKeys, actKeys);
  }

  @Test
  void testApplyRetentionPolicyForOneApplicableEntry() {
    var keys = List.of(buildDiagnosisKeyForDateTime(OffsetDateTime.now(UTC).minusDays(1L)));

    diagnosisKeyService.saveDiagnosisKeys(keys);
    diagnosisKeyService.applyRetentionPolicy(1, "DE");
    var actKeys = diagnosisKeyService.getDiagnosisKeys();

    assertDiagnosisKeysEqual(Lists.emptyList(), actKeys);
  }

  @Test
  void testShouldNotDeleteKeysFromAnotherCountry() {
    var expKeys = List.of(
        buildDiagnosisKeyForDateTime(OffsetDateTime.now(UTC).minusDays(1L), "DE", Collections.singletonList("DE")));

    diagnosisKeyService.saveDiagnosisKeys(expKeys);
    diagnosisKeyService.applyRetentionPolicy(1, "FR");
    var actKeys = diagnosisKeyService.getDiagnosisKeys();

    assertDiagnosisKeysEqual(actKeys, expKeys);
  }

  @Test
  void testShouldDeleteKeysWithMatchingVisitedCountry() {
    var frenchKeys = buildDiagnosisKeyForDateTime(OffsetDateTime.now(UTC).minusDays(1L), "DE", Collections.singletonList("FR"));
    var germanKeys = buildDiagnosisKeyForDateTime(OffsetDateTime.now(UTC).minusDays(2L), "DE", Collections.singletonList("DE"));

    diagnosisKeyService.saveDiagnosisKeys(List.of(germanKeys, frenchKeys));
    diagnosisKeyService.applyRetentionPolicy(1, "FR");
    var actKeys = diagnosisKeyService.getDiagnosisKeys();

    assertDiagnosisKeysEqual(List.of(germanKeys), actKeys);
  }

  @Test
  void testShouldDeleteKeysWhereAnyOfVisitedCountriesMatch() {
    var keys = List.of(
        buildDiagnosisKeyForDateTime(OffsetDateTime.now(UTC).minusDays(1L), "DE", List.of("DE", "FR", "LU")));
    diagnosisKeyService.saveDiagnosisKeys(keys);
    diagnosisKeyService.applyRetentionPolicy(1, "FR");
    var actKeys = diagnosisKeyService.getDiagnosisKeys();
    assertTrue(actKeys.isEmpty());
  }

  @Test
  void testShouldDeleteKeysFromDifferentOriginCountriesWithMatchingVisitedCountry() {
    var keys = List.of(
        buildDiagnosisKeyForDateTime(OffsetDateTime.now(UTC).minusDays(1L), "DE", List.of("FR")),
        buildDiagnosisKeyForDateTime(OffsetDateTime.now(UTC).minusDays(1L), "FR", List.of("FR")),
        buildDiagnosisKeyForDateTime(OffsetDateTime.now(UTC).minusDays(1L), "LU", List.of("FR")));
    diagnosisKeyService.saveDiagnosisKeys(keys);
    diagnosisKeyService.applyRetentionPolicy(1, "FR");
    var actKeys = diagnosisKeyService.getDiagnosisKeys();
    assertTrue(actKeys.isEmpty());
  }

  @Test
  void testNoPersistOnValidationError() {
    assertThat(catchThrowable(() -> {
      var keys = List.of(DiagnosisKey.builder()
          .withKeyData(new byte[16])
          .withRollingStartIntervalNumber((int) (OffsetDateTime.now(UTC).toEpochSecond() / 600))
          .withTransmissionRiskLevel(2)
          .withCountryCode("DE")
          .withVisitedCountries(Collections.singletonList("DE"))
          .withSubmissionTimestamp(0L).build());

      diagnosisKeyService.saveDiagnosisKeys(keys);
    })).isInstanceOf(InvalidDiagnosisKeyException.class);

    List<DiagnosisKey> actKeys = diagnosisKeyService.getDiagnosisKeys();

    assertDiagnosisKeysEqual(Lists.emptyList(), actKeys);
  }

  @Test
  void shouldNotUpdateExistingKey() {
    var keyData = "1234567890123456";
    var keys = List.of(DiagnosisKey.builder()
            .withKeyData(keyData.getBytes())
            .withRollingStartIntervalNumber(600)
            .withTransmissionRiskLevel(2)
            .withCountryCode("DE")
            .withVisitedCountries(Collections.singletonList("DE"))
            .withSubmissionTimestamp(0L).build(),
        DiagnosisKey.builder()
            .withKeyData(keyData.getBytes())
            .withRollingStartIntervalNumber(600)
            .withTransmissionRiskLevel(3)
            .withCountryCode("DE")
            .withVisitedCountries(Collections.singletonList("DE"))
            .withSubmissionTimestamp(0L).build());

    diagnosisKeyService.saveDiagnosisKeys(keys);

    var actKeys = diagnosisKeyService.getDiagnosisKeys();

    assertThat(actKeys.size()).isEqualTo(1);
    assertThat(actKeys.iterator().next().getTransmissionRiskLevel()).isEqualTo(2);
  }

  @Nested
  class TestRetrieveKeysFromVisitedCountry {

    @AfterEach
    public void tearDown() {
      diagnosisKeyRepository.deleteAll();
    }

    @BeforeEach
    public void setup() {
      var keys = List.of(
          buildDiagnosisKeyForDateTime(OffsetDateTime.now(UTC).minusDays(1L), "DE", Collections.singletonList("DE")),
          buildDiagnosisKeyForDateTime(OffsetDateTime.now(UTC).minusDays(2L), "DE", List.of("DE", "FR")),
          buildDiagnosisKeyForDateTime(OffsetDateTime.now(UTC).minusDays(3L), "DE", List.of("DE", "FR", "DK"))
      );
      diagnosisKeyService.saveDiagnosisKeys(keys);
    }

    @Test
    void testShouldGetThreeEntriesDE() {
      assertEquals(3, diagnosisKeyService.getDiagnosisKeysByVisitedCountry("DE").size());
    }

    @Test
    void testShouldGetTwoEntriesFR() {
      assertEquals(2, diagnosisKeyService.getDiagnosisKeysByVisitedCountry("FR").size());
    }

    @Test
    void testShouldGetOneEntryDK() {
      assertEquals(1, diagnosisKeyService.getDiagnosisKeysByVisitedCountry("DK").size());
    }
  }

}
