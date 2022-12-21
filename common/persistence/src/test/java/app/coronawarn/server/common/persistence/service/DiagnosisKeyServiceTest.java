package app.coronawarn.server.common.persistence.service;

import static app.coronawarn.server.common.persistence.service.DiagnosisKeyService.daysToSeconds;
import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.assertDiagnosisKeysEqual;
import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.buildDiagnosisKeyForDateTime;
import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.buildDiagnosisKeyForSubmissionTimestamp;
import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.generateRandomDiagnosisKey;
import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.generateRandomDiagnosisKeyWithSpecifiedTrl;
import static app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType.SUBMISSION_TYPE_PCR_TEST;
import static app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType.SUBMISSION_TYPE_RAPID_TEST;
import static app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType.SUBMISSION_TYPE_SRS_OTHER;
import static java.time.LocalDate.now;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.util.Lists.list;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;

@DataJdbcTest
class DiagnosisKeyServiceTest {

  public static final int MIN_TRL = 3;

  @Autowired
  private DiagnosisKeyService service;

  @Autowired
  private DiagnosisKeyRepository repo;

  @Test
  void ignoresRapidTestDiagnosisKeysWhenPcrTestIsPresent() {
    final DiagnosisKey pcrKey = generateRandomDiagnosisKey(true, 1, SUBMISSION_TYPE_PCR_TEST);
    service.saveDiagnosisKeys(list(pcrKey));
    Collection<DiagnosisKey> storedKeys = service.getDiagnosisKeys();
    assertEquals(1, storedKeys.size());
    assertTrue(storedKeys.contains(pcrKey));
    final DiagnosisKey rapidKey = DiagnosisKey.builder()
        .withKeyDataAndSubmissionType(pcrKey.getKeyData(), SUBMISSION_TYPE_RAPID_TEST)
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
    service.saveDiagnosisKeys(list(rapidKey));
    storedKeys = service.getDiagnosisKeys();
    assertEquals(1, storedKeys.size());
    assertTrue(storedKeys.contains(pcrKey));
    assertFalse(storedKeys.contains(rapidKey));
  }

  @Test
  void insertsPcrTestDiagnosisKeysWhenRapidTestIsPresent() {
    final DiagnosisKey pcrKey = generateRandomDiagnosisKey(true, 1, SUBMISSION_TYPE_PCR_TEST);
    final DiagnosisKey rapidKey = DiagnosisKey.builder()
        .withKeyDataAndSubmissionType(pcrKey.getKeyData(), SUBMISSION_TYPE_RAPID_TEST)
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
    service.saveDiagnosisKeys(list(rapidKey));
    Collection<DiagnosisKey> storedKeys = service.getDiagnosisKeys();
    assertEquals(1, storedKeys.size());
    assertTrue(storedKeys.contains(rapidKey));
    service.saveDiagnosisKeys(list(pcrKey));
    storedKeys = service.getDiagnosisKeys();
    assertEquals(2, storedKeys.size());
    assertTrue(storedKeys.contains(rapidKey));
    assertTrue(storedKeys.contains(pcrKey));
  }

  @ParameterizedTest
  @EnumSource(value = SubmissionType.class)
  void recordSrsTest(final SubmissionType type) {
    service.recordSrs(type);
  }

  @Test
  void shouldDoesntExist() {
    assertFalse(service.exists(null));
    assertFalse(service.exists(Collections.emptyList()));
  }

  @ParameterizedTest
  @ValueSource(ints = { 0, 1 })
  void shouldNotUpdateExistingKeyWithSameSubmissionType(final int submissionTypeNumber) {
    final var keyData = "1234567890123456";
    final var keys = list(DiagnosisKey.builder()
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

    final int actNumberOfInsertedRows = service.saveDiagnosisKeys(keys);
    final var actKeys = service.getDiagnosisKeys();

    assertThat(actNumberOfInsertedRows).isEqualTo(1);
    assertThat(actKeys).hasSize(1);
    assertThat(actKeys.iterator().next().getTransmissionRiskLevel()).isEqualTo(2);
  }

  /**
   * record 3 self reports for today, remove all older than yesterday, check that the ones from today are still there,
   * delete also from today, finally check that there are 0 for today.
   */
  @Test
  void srsTests() {
    service.recordSrs(SUBMISSION_TYPE_SRS_OTHER);
    service.recordSrs(SUBMISSION_TYPE_SRS_OTHER);
    service.recordSrs(SUBMISSION_TYPE_SRS_OTHER);
    assertEquals(3, repo.countSrsOlderThan(now(UTC).plusDays(1)));
    service.applySrsRetentionPolicy(1);
    assertEquals(3, repo.countSrsOlderThan(now(UTC).plusDays(1)));
    service.applySrsRetentionPolicy(0);
    assertEquals(0, repo.countSrsOlderThan(now(UTC).plusDays(1)));
  }

  @AfterEach
  public void tearDown() {
    repo.deleteAll();
  }

  @Test
  void testApplyRetentionPolicyForEmptyDb() {
    service.applyRetentionPolicy(1);
    final var actKeys = service.getDiagnosisKeys();
    assertThat(actKeys).isEmpty();
  }

  @DisplayName("Assert a negative retention period is rejected.")
  @ValueSource(ints = { Integer.MIN_VALUE, -1 })
  @ParameterizedTest
  void testApplyRetentionPolicyForNegativeNumberOfDays(final int daysToRetain) {
    assertThat(catchThrowable(() -> service.applyRetentionPolicy(daysToRetain)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void testApplyRetentionPolicyForOneApplicableEntry() {
    final var keys = list(buildDiagnosisKeyForDateTime(OffsetDateTime.now(UTC).minusDays(1L).minusHours(1)));

    service.saveDiagnosisKeys(keys);
    service.applyRetentionPolicy(1);
    final var actKeys = service.getDiagnosisKeys();

    assertThat(actKeys).isEmpty();
  }

  @Test
  void testApplyRetentionPolicyForOneNotApplicableEntry() {
    final var expKeys = list(buildDiagnosisKeyForDateTime(OffsetDateTime.now(UTC).minusDays(1L)));

    service.saveDiagnosisKeys(expKeys);
    service.applyRetentionPolicy(1);
    final var actKeys = service.getDiagnosisKeys();

    assertDiagnosisKeysEqual(expKeys, actKeys);
  }

  @DisplayName("Assert a positive retention period is accepted.")
  @ValueSource(ints = { 0, 1, Integer.MAX_VALUE })
  @ParameterizedTest
  void testApplyRetentionPolicyForValidNumberOfDays(final int daysToRetain) {
    assertThatCode(() -> service.applyRetentionPolicy(daysToRetain)).doesNotThrowAnyException();
  }

  @Test
  void testRetrievalForEmptyDB() {
    final var actKeys = service.getDiagnosisKeys();
    assertDiagnosisKeysEqual(emptyList(), actKeys);
  }

  @Test
  void testReturnedNumberOfInsertedKeysForNoConflict() {
    final var keys = list(buildDiagnosisKeyForSubmissionTimestamp(1L), buildDiagnosisKeyForSubmissionTimestamp(2L));

    service.saveDiagnosisKeys(keys);

    final var actKeys = service.getDiagnosisKeys();
    assertThat(actKeys).hasSize(2);
  }

  @Test
  void testSaveAndRetrieve() {
    final var expKeys = list(
        generateRandomDiagnosisKey(false, 1, SUBMISSION_TYPE_PCR_TEST),
        generateRandomDiagnosisKey(false, 1, SUBMISSION_TYPE_RAPID_TEST),
        generateRandomDiagnosisKey(true, 1, SUBMISSION_TYPE_PCR_TEST),
        generateRandomDiagnosisKey(true, 1, SUBMISSION_TYPE_RAPID_TEST));

    service.saveDiagnosisKeys(expKeys);
    final var actKeys = service.getDiagnosisKeys();

    assertEquals(4, actKeys.size());
    assertDiagnosisKeysEqual(expKeys, actKeys);
  }

  @Test
  void testSaveAndRetrieveKeysFilteredByTrl() {
    final var filterOutKeysBasedOnTrl = list(
        generateRandomDiagnosisKeyWithSpecifiedTrl(false, daysToSeconds(1), SUBMISSION_TYPE_PCR_TEST, 1),
        generateRandomDiagnosisKeyWithSpecifiedTrl(false, daysToSeconds(1), SUBMISSION_TYPE_RAPID_TEST, 2));

    final var expKeys = list(
        generateRandomDiagnosisKeyWithSpecifiedTrl(true, daysToSeconds(1), SUBMISSION_TYPE_PCR_TEST, 3),
        generateRandomDiagnosisKeyWithSpecifiedTrl(true, daysToSeconds(1), SUBMISSION_TYPE_RAPID_TEST, 4));

    final var oldKeys = list(
        generateRandomDiagnosisKeyWithSpecifiedTrl(true, daysToSeconds(42), SUBMISSION_TYPE_PCR_TEST, 3),
        generateRandomDiagnosisKeyWithSpecifiedTrl(true, daysToSeconds(42), SUBMISSION_TYPE_RAPID_TEST, 4));

    service.saveDiagnosisKeys(filterOutKeysBasedOnTrl);
    service.saveDiagnosisKeys(oldKeys);
    service.saveDiagnosisKeys(expKeys);

    final var actKeys = service.getDiagnosisKeysWithMinTrl(MIN_TRL, 10);

    assertEquals(2, actKeys.size());
    assertDiagnosisKeysEqual(expKeys, actKeys);
  }

  @Test
  void testSortedRetrievalResult() {
    final var expKeys = list(buildDiagnosisKeyForSubmissionTimestamp(2L), buildDiagnosisKeyForSubmissionTimestamp(1L));

    service.saveDiagnosisKeys(expKeys);

    // reverse to match expected sort order
    Collections.reverse(expKeys);
    final var actKeys = service.getDiagnosisKeys();

    assertDiagnosisKeysEqual(expKeys, actKeys);
  }
}
