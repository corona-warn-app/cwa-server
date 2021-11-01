package app.coronawarn.server.common.persistence.service.common;

import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.getKeySubmittedHoursAfterMidnightExpiration;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.of;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.junit.DisabledAroundMidnight;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DiagnosisKeyExpirationCheckerTest {

  /**
   * Midnight - start of this day.
   */
  private static final LocalDateTime MN = LocalDateTime.of(LocalDate.now(ZoneOffset.UTC), LocalTime.MIDNIGHT);

  private static Stream<Arguments> expiredKeysDataset() {
    return Stream.of(
        of(getKeySubmittedHoursAfterMidnightExpiration(1), ExpirationPolicy.of(60, ChronoUnit.MINUTES),
            MN.plusHours(2)),
        of(getKeySubmittedHoursAfterMidnightExpiration(2), ExpirationPolicy.of(60, ChronoUnit.MINUTES),
            MN.plusHours(3)),
        of(getKeySubmittedHoursAfterMidnightExpiration(2), ExpirationPolicy.of(120, ChronoUnit.MINUTES),
            MN.plusHours(3)),
        of(getKeySubmittedHoursAfterMidnightExpiration(4), ExpirationPolicy.of(120, ChronoUnit.MINUTES),
            MN.plusHours(4)));
  }

  private static Stream<Arguments> notExpiredKeysDataset() {
    return Stream.of(
        of(getKeySubmittedHoursAfterMidnightExpiration(1), ExpirationPolicy.of(120, ChronoUnit.MINUTES),
            MN.plusHours(1).plusMinutes(30)),
        of(getKeySubmittedHoursAfterMidnightExpiration(2), ExpirationPolicy.of(180, ChronoUnit.MINUTES),
            MN.plusHours(2).plusMinutes(30)),
        of(getKeySubmittedHoursAfterMidnightExpiration(3), ExpirationPolicy.of(240, ChronoUnit.MINUTES),
            MN.plusHours(3).plusMinutes(30)));
  }

  private final KeySharingPoliciesChecker sharingPoliciesChecker = new KeySharingPoliciesChecker();

  @ParameterizedTest
  @MethodSource("expiredKeysDataset")
  @DisabledAroundMidnight(offsetInMinutes = 4 * 60 + 1)
  void shouldComputeThatKeyIsExpired(DiagnosisKey key, ExpirationPolicy expirationPolicy, LocalDateTime shareTime) {
    assertTrue(sharingPoliciesChecker.canShareKeyAtTime(key, expirationPolicy, shareTime));
  }

  @ParameterizedTest
  @MethodSource("notExpiredKeysDataset")
  @DisabledAroundMidnight(offsetInMinutes = 3 * 60 + 1)
  void shouldComputeThatKeyIsNotExpired(DiagnosisKey key, ExpirationPolicy expirationPolicy, LocalDateTime shareTime) {
    assertFalse(sharingPoliciesChecker.canShareKeyAtTime(key, expirationPolicy, shareTime));
  }
}
