package app.coronawarn.server.common.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;

@DataJdbcTest
class EnsureSrsIsNotSendToEfgsTest {

  @Autowired
  private DiagnosisKeyRepository repository;

  @Autowired
  private FederationUploadKeyRepository efgs;

  @BeforeEach
  void beforeEach() {
    repository.deleteAll();
    efgs.deleteAll();
  }

  @ParameterizedTest
  @EnumSource(value = SubmissionType.class, names = { "SUBMISSION_TYPE_SRS_.*" }, mode = Mode.MATCH_ANY)
  void checkSrsKeysWontBeUploaded(final SubmissionType type) {
    final byte[] id = new byte[16];
    new Random().nextBytes(id);

    assertFalse(repository.exists(id, type.name()));

    final DiagnosisKey key = DiagnosisKey.builder()
        .withKeyDataAndSubmissionType(id, type)
        .withRollingStartIntervalNumber(600)
        .withTransmissionRiskLevel(5)
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
    assertThat(efgs.findAll()).doesNotContain(FederationUploadKey.from(key));
  }
}
