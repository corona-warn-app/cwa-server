

package app.coronawarn.server.common.persistence.service.common;

import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.getKeySubmittedHoursAfterMidnightExpiration;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;

class DiagnosisKeyExpirationCheckerTest {

  private final KeySharingPoliciesChecker sharingPoliciesChecker = new KeySharingPoliciesChecker();


  @ParameterizedTest
  @MethodSource("notExpiredKeysDataset")
  void shouldComputeThatKeyIsNotExpired(DiagnosisKey key, ExpirationPolicy expirationPolicy, LocalDateTime shareTime) {
    assertFalse(sharingPoliciesChecker.canShareKeyAtTime(key, expirationPolicy, shareTime));
  }

  @ParameterizedTest
  @MethodSource("expiredKeysDataset")
  void shouldComputeThatKeyIsExpired(DiagnosisKey key, ExpirationPolicy expirationPolicy, LocalDateTime shareTime) {
    assertTrue(sharingPoliciesChecker.canShareKeyAtTime(key, expirationPolicy, shareTime));
  }

  private static Stream<Arguments> expiredKeysDataset() {
    LocalDateTime midnight = LocalDateTime.of(LocalDate.now(ZoneOffset.UTC), LocalTime.MIDNIGHT);
    return Stream.of(
        Arguments.of(getKeySubmittedHoursAfterMidnightExpiration(1), ExpirationPolicy.of(60, ChronoUnit.MINUTES), midnight.plusHours(2)),
        Arguments.of(getKeySubmittedHoursAfterMidnightExpiration(2), ExpirationPolicy.of(60, ChronoUnit.MINUTES), midnight.plusHours(3)),
        Arguments.of(getKeySubmittedHoursAfterMidnightExpiration(2), ExpirationPolicy.of(120, ChronoUnit.MINUTES), midnight.plusHours(3)),
        Arguments.of(getKeySubmittedHoursAfterMidnightExpiration(4), ExpirationPolicy.of(120, ChronoUnit.MINUTES), midnight.plusHours(4))
    );
  }

  private static Stream<Arguments> notExpiredKeysDataset() {
    LocalDateTime midnight = LocalDateTime.of(LocalDate.now(ZoneOffset.UTC), LocalTime.MIDNIGHT);
    return Stream.of(
        Arguments.of(getKeySubmittedHoursAfterMidnightExpiration(1), ExpirationPolicy.of(120, ChronoUnit.MINUTES), midnight.plusHours(1).plusMinutes(30)),
        Arguments.of(getKeySubmittedHoursAfterMidnightExpiration(2), ExpirationPolicy.of(180, ChronoUnit.MINUTES), midnight.plusHours(2).plusMinutes(30)),
        Arguments.of(getKeySubmittedHoursAfterMidnightExpiration(3), ExpirationPolicy.of(240, ChronoUnit.MINUTES), midnight.plusHours(3).plusMinutes(30))
    );
  }
}
