package app.coronawarn.server.common.persistence.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;

@DataJdbcTest
public class DiagnosisKeyRepositoryTest {

  @Autowired
  private DiagnosisKeyRepository repository;

  private byte[] id = new byte[16];
  private SubmissionType type = SubmissionType.SUBMISSION_TYPE_PCR_TEST;

  @AfterEach
  void tearDown() {
    repository.deleteAll();
  }

  @Test
  void shouldCheckExistence() {

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

  @Test
  void shouldNotAddAnInvalidDiagnosisKey() {

    Exception exception = assertThrows(InvalidDiagnosisKeyException.class, () -> {
      DiagnosisKey.builder()
          .withKeyDataAndSubmissionType(id, type)
          .withRollingStartIntervalNumber(600)
          .withTransmissionRiskLevel(2)
          .withRollingPeriod(0)
          .withCountryCode("DE")
          .withVisitedCountries(Set.of("DE"))
          .withSubmissionTimestamp(0L)
          .withReportType(ReportType.CONFIRMED_TEST).build();
    });

    String actualMessage = exception.getMessage();
    assertEquals("[Rolling period must be between 1 and 144. Invalid Value: 0]", actualMessage);
  }
}
