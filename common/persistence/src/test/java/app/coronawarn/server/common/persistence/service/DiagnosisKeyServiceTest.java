

package app.coronawarn.server.common.persistence.service;

import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.assertDiagnosisKeysEqual;
import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.buildDiagnosisKeyForDateTime;
import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.buildDiagnosisKeyForSubmissionTimestamp;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.util.Lists.list;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;

@DataJdbcTest
class DiagnosisKeyServiceTest {

  public static final int MIN_TRL = 3;
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
    assertDiagnosisKeysEqual(emptyList(), actKeys);
  }

  @Test
  void testSaveAndRetrieve() {
    var expKeys = List.of(
        DiagnosisKeyServiceTestHelper.generateRandomDiagnosisKey(false, 1, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        DiagnosisKeyServiceTestHelper.generateRandomDiagnosisKey(false, 1, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        DiagnosisKeyServiceTestHelper.generateRandomDiagnosisKey(true, 1, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        DiagnosisKeyServiceTestHelper.generateRandomDiagnosisKey(true, 1, SubmissionType.SUBMISSION_TYPE_RAPID_TEST)
    );

    diagnosisKeyService.saveDiagnosisKeys(expKeys);
    var actKeys = diagnosisKeyService.getDiagnosisKeys();

    assertEquals(4, actKeys.size());
    assertDiagnosisKeysEqual(expKeys, actKeys);
  }

  @Test
  void testSaveAndRetrieveKeysFilteredByTrl() {
    var filterOutKeysBasedOnTrl =List.of(
        DiagnosisKeyServiceTestHelper.generateRandomDiagnosisKeyWithSpecifiedTrl(false, 1,
            SubmissionType.SUBMISSION_TYPE_PCR_TEST, 1),
    DiagnosisKeyServiceTestHelper.generateRandomDiagnosisKeyWithSpecifiedTrl(false, 1,
        SubmissionType.SUBMISSION_TYPE_RAPID_TEST, 2));
    var expKeys = List.of(
        DiagnosisKeyServiceTestHelper.generateRandomDiagnosisKeyWithSpecifiedTrl(true, 1,
            SubmissionType.SUBMISSION_TYPE_PCR_TEST, 3),
        DiagnosisKeyServiceTestHelper.generateRandomDiagnosisKeyWithSpecifiedTrl(true, 1,
            SubmissionType.SUBMISSION_TYPE_RAPID_TEST, 4)
    );

    diagnosisKeyService.saveDiagnosisKeys(filterOutKeysBasedOnTrl);
    diagnosisKeyService.saveDiagnosisKeys(expKeys);

    var actKeys = diagnosisKeyService.getDiagnosisKeysWithMinTrl(MIN_TRL);

    assertEquals(2, actKeys.size());
    assertDiagnosisKeysEqual(expKeys, actKeys);
  }

  @Test
  void testSortedRetrievalResult() {
    var expKeys = list(
        buildDiagnosisKeyForSubmissionTimestamp(2L),
        buildDiagnosisKeyForSubmissionTimestamp(1L));

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

  @ParameterizedTest
  @ValueSource(ints = {0, 1})
  void shouldNotUpdateExistingKeyWithSameSubmissionType(int submissionTypeNumber) {
    var keyData = "1234567890123456";
    var keys = list(DiagnosisKey.builder()
            .withKeyDataAndSubmissionType(keyData.getBytes(), SubmissionType.forNumber(submissionTypeNumber))
            .withRollingStartIntervalNumber(600)
            .withTransmissionRiskLevel(2)
            .withCountryCode("DE")
            .withVisitedCountries(Set.of("DE"))
            .withSubmissionTimestamp(0L)
            .withReportType(ReportType.CONFIRMED_TEST)
            .build(),
        DiagnosisKey.builder()
            .withKeyDataAndSubmissionType(keyData.getBytes(), SubmissionType.forNumber(submissionTypeNumber))
            .withRollingStartIntervalNumber(600)
            .withTransmissionRiskLevel(3)
            .withCountryCode("DE")
            .withVisitedCountries(Set.of("DE"))
            .withSubmissionTimestamp(0L)
            .withReportType(ReportType.CONFIRMED_TEST)
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
        buildDiagnosisKeyForSubmissionTimestamp(2L));

    diagnosisKeyService.saveDiagnosisKeys(keys);

    var actKeys = diagnosisKeyService.getDiagnosisKeys();
    assertThat(actKeys).hasSize(2);
  }

  @Test
  void insertsPcrTestDiagnosisKeysWhenRapidTestIsPresent() {
    DiagnosisKey pcrKey = DiagnosisKeyServiceTestHelper
        .generateRandomDiagnosisKey(true, 1, SubmissionType.SUBMISSION_TYPE_PCR_TEST);
    DiagnosisKey rapidKey = DiagnosisKey.builder()
        .withKeyDataAndSubmissionType(pcrKey.getKeyData(), SubmissionType.SUBMISSION_TYPE_RAPID_TEST)
        .withRollingStartIntervalNumber(pcrKey.getRollingStartIntervalNumber())
        .withTransmissionRiskLevel(pcrKey.getTransmissionRiskLevel())
        .withConsentToFederation(pcrKey.isConsentToFederation())
        .withCountryCode(pcrKey.getOriginCountry())
        .withDaysSinceOnsetOfSymptoms(pcrKey.getDaysSinceOnsetOfSymptoms())
        .withReportType(pcrKey.getReportType())
        .withRollingPeriod(pcrKey.getRollingPeriod())
        .withSubmissionTimestamp(pcrKey.getSubmissionTimestamp())
        .withVisitedCountries(pcrKey.getVisitedCountries())
        .build();
    Collection<DiagnosisKey> storedKeys;
    diagnosisKeyService.saveDiagnosisKeys(List.of(rapidKey));
    storedKeys = diagnosisKeyService.getDiagnosisKeys();
    assertEquals(1, storedKeys.size());
    assertTrue(storedKeys.contains(rapidKey));
    diagnosisKeyService.saveDiagnosisKeys(List.of(pcrKey));
    storedKeys = diagnosisKeyService.getDiagnosisKeys();
    assertEquals(2, storedKeys.size());
    assertTrue(storedKeys.contains(rapidKey));
    assertTrue(storedKeys.contains(pcrKey));
  }

  @Test
  void ignoresRapidTestDiagnosisKeysWhenPcrTestIsPresent() {
    DiagnosisKey pcrKey = DiagnosisKeyServiceTestHelper
        .generateRandomDiagnosisKey(true, 1, SubmissionType.SUBMISSION_TYPE_PCR_TEST);
    DiagnosisKey rapidKey = DiagnosisKey.builder()
        .withKeyDataAndSubmissionType(pcrKey.getKeyData(), SubmissionType.SUBMISSION_TYPE_RAPID_TEST)
        .withRollingStartIntervalNumber(pcrKey.getRollingStartIntervalNumber())
        .withTransmissionRiskLevel(pcrKey.getTransmissionRiskLevel())
        .withConsentToFederation(pcrKey.isConsentToFederation())
        .withCountryCode(pcrKey.getOriginCountry())
        .withDaysSinceOnsetOfSymptoms(pcrKey.getDaysSinceOnsetOfSymptoms())
        .withReportType(pcrKey.getReportType())
        .withRollingPeriod(pcrKey.getRollingPeriod())
        .withSubmissionTimestamp(pcrKey.getSubmissionTimestamp())
        .withVisitedCountries(pcrKey.getVisitedCountries())
        .build();
    Collection<DiagnosisKey> storedKeys;
    diagnosisKeyService.saveDiagnosisKeys(List.of(pcrKey));
    storedKeys = diagnosisKeyService.getDiagnosisKeys();
    assertEquals(1, storedKeys.size());
    assertTrue(storedKeys.contains(pcrKey));
    diagnosisKeyService.saveDiagnosisKeys(List.of(rapidKey));
    storedKeys = diagnosisKeyService.getDiagnosisKeys();
    assertEquals(1, storedKeys.size());
    assertTrue(storedKeys.contains(pcrKey));
    assertFalse(storedKeys.contains(rapidKey));
  }
}
