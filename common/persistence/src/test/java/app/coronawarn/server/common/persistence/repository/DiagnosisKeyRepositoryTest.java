package app.coronawarn.server.common.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import java.time.LocalDate;
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

  @Autowired
  private DiagnosisKeyRepository repository;

  @AfterEach
  void tearDown() {
    repository.deleteAll();
  }

  @Test
  void shouldCheckExistence() {

    SubmissionType type = SubmissionType.SUBMISSION_TYPE_PCR_TEST;
    byte[] id = new byte[16];
    new Random().nextBytes(id);

    assertFalse(repository.exists(id, type.name()));

    DiagnosisKey key = DiagnosisKey.builder()
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

  @ParameterizedTest
  @EnumSource(value = SubmissionType.class, names = { "SUBMISSION_TYPE_SRS_.*" }, mode = Mode.MATCH_ANY)
  void recordSrsTest(final SubmissionType type) {
    assertTrue(repository.recordSrs(type.name()));
    assertEquals(1, repository.countTodaysSrs());
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    assertEquals(1, repository.countSrsOlderThan(tomorrow));
    repository.deleteSrsOlderThan(tomorrow);
    assertEquals(0, repository.countTodaysSrs());
    assertEquals(0, repository.countSrsOlderThan(tomorrow));
  }
}
