package app.coronawarn.server.common.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;

@DataJdbcTest
class DiagnosisKeyRepositoryTest {

  public static DiagnosisKey randomKey() {
    final SubmissionType type = SubmissionType.SUBMISSION_TYPE_PCR_TEST;
    final byte[] id = new byte[16];
    new Random().nextBytes(id);

    return DiagnosisKey.builder()
        .withKeyDataAndSubmissionType(id, type)
        .withRollingStartIntervalNumber(600)
        .withTransmissionRiskLevel(2)
        .withRollingPeriod(1)
        .withSubmissionTimestamp(0L)
        .withCountryCode("DE")
        .build();
  }

  @Autowired
  private DiagnosisKeyRepository repository;

  @Test
  void checkKeyData() {
    final DiagnosisKey key1 = randomKey();
    final DiagnosisKey key2 = randomKey();
    final DiagnosisKey key3 = randomKey();
    final DiagnosisKey key4 = randomKey();
    assertFalse(repository.exists(key1.getKeyData(), key1.getSubmissionType().name()));
    assertFalse(repository.exists(key2.getKeyData(), key2.getSubmissionType().name()));
    assertFalse(repository.exists(key3.getKeyData(), key3.getSubmissionType().name()));
    assertFalse(repository.exists(key4.getKeyData(), key4.getSubmissionType().name()));
    save(key1);
    assertTrue(repository.exists(List.of(key1.getKeyData())));
    assertFalse(repository.exists(List.of(key2.getKeyData(), key3.getKeyData(), key4.getKeyData())));
    save(key2);
    assertTrue(repository.exists(List.of(key2.getKeyData())));
    assertFalse(repository.exists(List.of(key3.getKeyData(), key4.getKeyData())));
    save(key3);
    save(key4);
    assertTrue(repository.exists(List.of(key1.getKeyData(), key2.getKeyData(), key3.getKeyData(), key4.getKeyData())));
  }

  @ParameterizedTest
  @EnumSource(value = SubmissionType.class, names = { "SUBMISSION_TYPE_SRS_.*" }, mode = Mode.MATCH_ANY)
  void recordSrsTest(final SubmissionType type) {
    assertTrue(repository.recordSrs(type.name()));
    final LocalDate tomorrow = LocalDate.now().plusDays(1);
    assertEquals(1, repository.countSrsOlderThan(tomorrow));
    repository.deleteSrsOlderThan(tomorrow);
    assertEquals(0, repository.countSrsOlderThan(tomorrow));
  }

  void save(final DiagnosisKey key) {
    repository.saveDoNothingOnConflict(
        key.getKeyData(), key.getRollingStartIntervalNumber(), key.getRollingPeriod(),
        key.getSubmissionTimestamp(), key.getTransmissionRiskLevel(),
        null, null, null, 0,
        key.isConsentToFederation(), key.getSubmissionType().name());
  }

  @Test
  void shouldCheckExistence() {
    final SubmissionType type = SubmissionType.SUBMISSION_TYPE_PCR_TEST;
    final byte[] id = new byte[16];
    new Random().nextBytes(id);

    assertFalse(repository.exists(id, type.name()));

    final DiagnosisKey key = DiagnosisKey.builder()
        .withKeyDataAndSubmissionType(id, type)
        .withRollingStartIntervalNumber(600)
        .withTransmissionRiskLevel(2)
        .withRollingPeriod(1)
        .withCountryCode("DE")
        .withVisitedCountries(Set.of("DE"))
        .withSubmissionTimestamp(0L)
        .withReportType(ReportType.CONFIRMED_TEST)
        .build();
    repository.saveDoNothingOnConflict(
        key.getKeyData(), key.getRollingStartIntervalNumber(), key.getRollingPeriod(),
        key.getSubmissionTimestamp(), key.getTransmissionRiskLevel(),
        key.getOriginCountry(), key.getVisitedCountries().toArray(new String[0]),
        key.getReportType().name(), key.getDaysSinceOnsetOfSymptoms(),
        key.isConsentToFederation(), key.getSubmissionType().name());

    assertTrue(repository.exists(id, type.name()));
  }

  @AfterEach
  void tearDown() {
    repository.deleteAll();
  }
}
