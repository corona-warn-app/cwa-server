

package app.coronawarn.server.common.persistence.domain;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DiagnosisKeyTest {

  final static byte[] expKeyData = "testKey111111111".getBytes(StandardCharsets.US_ASCII);
  final static int expRollingStartIntervalNumber = 1;
  final static int expRollingPeriod = 2;
  final static int expTransmissionRiskLevel = 3;
  final static long expSubmissionTimestamp = 4L;
  static final String originCountry = "DE";
  static final Set<String> visitedCountries = Set.of("DE");
  static final ReportType reportType = ReportType.CONFIRMED_TEST;
  static final int daysSinceOnsetOfSymptoms = 1;

  final static DiagnosisKey diagnosisKey = new DiagnosisKey(expKeyData, expRollingStartIntervalNumber,
      expRollingPeriod, expTransmissionRiskLevel, expSubmissionTimestamp, false, originCountry, visitedCountries,
      reportType, daysSinceOnsetOfSymptoms);

  @Test
  void testRollingStartIntervalNumberGetter() {
    assertThat(diagnosisKey.getRollingStartIntervalNumber()).isEqualTo(expRollingStartIntervalNumber);
  }

  @Test
  void testRollingPeriodGetter() {
    assertThat(diagnosisKey.getRollingPeriod()).isEqualTo(expRollingPeriod);
  }

  @Test
  void testTransmissionRiskLevelGetter() {
    assertThat(diagnosisKey.getTransmissionRiskLevel()).isEqualTo(expTransmissionRiskLevel);
  }

  @Test
  void testSubmissionTimestampGetter() {
    assertThat(diagnosisKey.getSubmissionTimestamp()).isEqualTo(expSubmissionTimestamp);
  }

  @Test
  void testIsYoungerThanRetentionThreshold() {
    int fiveDaysAgo = (int) (LocalDateTime
        .ofInstant(Instant.now(), UTC)
        .minusDays(5).minusMinutes(10)
        .toEpochSecond(UTC) / (60 * 10));
    DiagnosisKey diagnosisKeyFiveDays = new DiagnosisKey(expKeyData, fiveDaysAgo,
        expRollingPeriod, expTransmissionRiskLevel, expSubmissionTimestamp, false, originCountry, visitedCountries,
        reportType, daysSinceOnsetOfSymptoms);

    assertThat(diagnosisKeyFiveDays.isYoungerThanRetentionThreshold(4)).isFalse();
    assertThat(diagnosisKeyFiveDays.isYoungerThanRetentionThreshold(5)).isFalse();
    assertThat(diagnosisKeyFiveDays.isYoungerThanRetentionThreshold(6)).isTrue();
  }

  @DisplayName("Test retention threshold accepts positive value")
  @ValueSource(ints = {0, 1, Integer.MAX_VALUE})
  @ParameterizedTest
  void testRetentionThresholdAcceptsPositiveValue(int daysToRetain) {
    assertThatCode(() -> diagnosisKey.isYoungerThanRetentionThreshold(daysToRetain))
        .doesNotThrowAnyException();
  }

  @DisplayName("Test retention threshold rejects negative value")
  @ValueSource(ints = {Integer.MIN_VALUE, -1})
  @ParameterizedTest
  void testRetentionThresholdRejectsNegativeValue(int daysToRetain) {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> diagnosisKey.isYoungerThanRetentionThreshold(daysToRetain));
  }
}
