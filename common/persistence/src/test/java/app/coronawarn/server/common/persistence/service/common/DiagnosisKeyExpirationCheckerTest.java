package app.coronawarn.server.common.persistence.service.common;

import static java.time.ZoneOffset.UTC;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.buildDiagnosisKeyForSubmissionTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import app.coronawarn.server.common.persistence.domain.DiagnosisKey;

class DiagnosisKeyExpirationCheckerTest {

  private final DiagnosisKeyExpirationChecker expirationChecker = new DiagnosisKeyExpirationChecker();


  @ParameterizedTest
  @MethodSource("notExpiredKeysDataset")
  void shouldComputeThatKeyIsNotExpired(DiagnosisKey key, ExpirationPolicy expirationPolicy) {
    assertFalse(expirationChecker.isKeyExpiredForPolicy(key, expirationPolicy));
  }

  @ParameterizedTest
  @MethodSource("expiredKeysDataset")
  void shouldComputeThatKeyIsExpired(DiagnosisKey key, ExpirationPolicy expirationPolicy) {
    assertTrue(expirationChecker.isKeyExpiredForPolicy(key, expirationPolicy));
  }

  private static Stream<Arguments> expiredKeysDataset() {
    return Stream.of(
        Arguments.of(getKeySubmittedHoursAfterExpiration(1), ExpirationPolicy.of(60, ChronoUnit.MINUTES)),
        Arguments.of(getKeySubmittedHoursAfterExpiration(2), ExpirationPolicy.of(60, ChronoUnit.MINUTES)),
        Arguments.of(getKeySubmittedHoursAfterExpiration(2), ExpirationPolicy.of(120, ChronoUnit.MINUTES)),
        Arguments.of(getKeySubmittedHoursAfterExpiration(4), ExpirationPolicy.of(120, ChronoUnit.MINUTES))
    );
  }

  private static Stream<Arguments> notExpiredKeysDataset() {
    return Stream.of(
        Arguments.of(getKeySubmittedHoursAfterExpiration(1), ExpirationPolicy.of(120, ChronoUnit.MINUTES)),
        Arguments.of(getKeySubmittedHoursAfterExpiration(2), ExpirationPolicy.of(180, ChronoUnit.MINUTES)),
        Arguments.of(getKeySubmittedHoursAfterExpiration(3), ExpirationPolicy.of(240, ChronoUnit.MINUTES))
    );
  }

  /**
   * @return A key that is submitted hours after rollout period has passed.
   * Altough the application uses minutes for expiration policies, the submission times are
   * computed relative to the top of the hours.
   */
  private static DiagnosisKey getKeySubmittedHoursAfterExpiration(int hours) {
    LocalDateTime yesterday = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).minusDays(1);

    // key rolled out yesterday (relative to the test run) at 00:00
    int rollingStart = Math.toIntExact(yesterday.toEpochSecond(UTC) / 600L);
    LocalDateTime rollingPeriodExpiryTime = new DiagnosisKeyExpirationChecker()
        .calculateRollingPeriodExpiryTime(rollingStart, 144);

    // submission time is minutes after rolling period has passed
    long submissionTime = rollingPeriodExpiryTime.plus(hours, ChronoUnit.HOURS)
            .toEpochSecond(UTC) / TimeUnit.HOURS.toSeconds(1);

    return buildDiagnosisKeyForSubmissionTimestamp(submissionTime, rollingStart, true);
  }
}
