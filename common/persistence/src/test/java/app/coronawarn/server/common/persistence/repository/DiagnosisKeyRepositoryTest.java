package app.coronawarn.server.common.persistence.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;

@DataJdbcTest
public class DiagnosisKeyRepositoryTest {

  @Autowired
  private DiagnosisKeyService service;

  @Autowired
  private DiagnosisKeyRepository repository;

  @AfterEach
  void tearDown() {
    repository.deleteAll();
  }

  @Test
  void shouldCheckExistence() {

    byte[] id = new byte[16];
    new Random().nextBytes(id);
    SubmissionType type = SubmissionType.SUBMISSION_TYPE_PCR_TEST;

    assertFalse(repository.exists(id, type.name()));

    DiagnosisKey key = DiagnosisKey.builder()
        .withKeyDataAndSubmissionType(id, type)
        .withRollingStartIntervalNumber(600)
        .withTransmissionRiskLevel(2)
        .withCountryCode("DE")
        .withVisitedCountries(Set.of("DE"))
        .withSubmissionTimestamp(0L)
        .withReportType(ReportType.CONFIRMED_TEST)
        .build();
    service.saveDiagnosisKeys(List.of(key));

    assertTrue(repository.exists(id, type.name()));
  }
}


// additional checks -> when inserting zero rolling period is not working properly
// try to insert a diagn key
// expectation: you can not insert
