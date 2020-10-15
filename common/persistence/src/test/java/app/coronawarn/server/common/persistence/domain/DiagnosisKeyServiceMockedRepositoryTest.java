

package app.coronawarn.server.common.persistence.domain;

import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.assertDiagnosisKeysEqual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@DataJdbcTest
class DiagnosisKeyServiceMockedRepositoryTest {

  static final byte[] expKeyData = "16-bytelongarray".getBytes(StandardCharsets.US_ASCII);
  static final int expRollingStartIntervalNumber = 73800;
  static final int expTransmissionRiskLevel = 1;
  static final String originCountry = "DE";
  static final Set<String> visitedCountries = Set.of("DE");
  static final ReportType reportType = ReportType.CONFIRMED_TEST;
  static final int daysSinceOnsetOfSymptoms = 1;

  @Autowired
  private DiagnosisKeyService diagnosisKeyService;

  @MockBean
  private DiagnosisKeyRepository diagnosisKeyRepository;

  @Test
  void testKeyRetrievalWithInvalidDbEntries() {
    DiagnosisKey invalidKey1 = invalidKey(1L);
    DiagnosisKey invalidKey2 = invalidKey(3L);

    mockInvalidKeyInDb(list(invalidKey1, invalidKey2));

    List<DiagnosisKey> actualKeys = diagnosisKeyService.getDiagnosisKeys();
    assertThat(actualKeys).isEmpty();
  }

  @Test
  void testKeyRetrievalWithInvalidAndValidDbEntries() {
    DiagnosisKey invalidKey1 = invalidKey(1L);
    DiagnosisKey invalidKey2 = invalidKey(3L);
    var expKeys = list(
        validKey(2L),
        invalidKey1,
        validKey(0L),
        invalidKey2);

    mockInvalidKeyInDb(expKeys);

    List<DiagnosisKey> actualKeys = diagnosisKeyService.getDiagnosisKeys();
    expKeys.remove(invalidKey1);
    expKeys.remove(invalidKey2);
    assertDiagnosisKeysEqual(expKeys, actualKeys);
  }

  private void mockInvalidKeyInDb(List<DiagnosisKey> keys) {
    when(diagnosisKeyRepository.findAll(Sort.by(Direction.ASC, "submissionTimestamp"))).thenReturn(keys);
  }

  private DiagnosisKey validKey(long expSubmissionTimestamp) {
    return new DiagnosisKey(expKeyData, expRollingStartIntervalNumber,
        DiagnosisKey.MAX_ROLLING_PERIOD, expTransmissionRiskLevel, expSubmissionTimestamp, false,
        originCountry, visitedCountries, reportType, daysSinceOnsetOfSymptoms);
  }

  private DiagnosisKey invalidKey(long expSubmissionTimestamp) {
    byte[] expKeyData = "17--bytelongarray".getBytes(StandardCharsets.US_ASCII);
    return new DiagnosisKey(expKeyData, expRollingStartIntervalNumber,
        DiagnosisKey.MAX_ROLLING_PERIOD, expTransmissionRiskLevel, expSubmissionTimestamp, false,
        originCountry, visitedCountries, reportType, daysSinceOnsetOfSymptoms);
  }
}
