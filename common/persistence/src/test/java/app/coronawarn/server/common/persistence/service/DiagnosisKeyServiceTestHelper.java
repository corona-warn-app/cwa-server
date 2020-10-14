

package app.coronawarn.server.common.persistence.service;

import static app.coronawarn.server.common.persistence.domain.DiagnosisKey.ROLLING_PERIOD_MINUTES_INTERVAL;
import static app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker.TEN_MINUTES_INTERVAL_SECONDS;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;

public class DiagnosisKeyServiceTestHelper {

  private static final Random random = new Random();


  public static void assertDiagnosisKeysEqual(List<DiagnosisKey> expKeys,
      List<DiagnosisKey> actKeys) {
    assertThat(actKeys).withFailMessage("Cardinality mismatch").hasSameSizeAs(expKeys);

    for (int i = 0; i < expKeys.size(); i++) {
      var expKey = expKeys.get(i);
      var actKey = actKeys.get(i);

      assertDiagnosisKeysEqual(expKey, actKey);
    }
  }

  public static void assertDiagnosisKeysEqual(DiagnosisKey expKey, DiagnosisKey actKey) {
    assertThat(actKey.getKeyData()).withFailMessage("keyData mismatch")
        .isEqualTo(expKey.getKeyData());
    assertThat(actKey.getRollingStartIntervalNumber()).withFailMessage("rollingStartIntervalNumber mismatch")
        .isEqualTo(expKey.getRollingStartIntervalNumber());
    assertThat(actKey.getRollingPeriod()).withFailMessage("rollingPeriod mismatch")
        .isEqualTo(expKey.getRollingPeriod());
    assertThat(actKey.getTransmissionRiskLevel())
        .withFailMessage("transmissionRiskLevel mismatch")
        .isEqualTo(expKey.getTransmissionRiskLevel());
    assertThat(actKey.getSubmissionTimestamp()).withFailMessage("submissionTimestamp mismatch")
        .isEqualTo(expKey.getSubmissionTimestamp());
  }

  public static DiagnosisKey buildDiagnosisKeyForSubmissionTimestamp(long submissionTimeStamp, int rollingStartInterval,
      boolean consentToFederation, String countryCode, Set<String> visitedCountries, ReportType reportType) {
    byte[] randomBytes = new byte[16];
    random.nextBytes(randomBytes);
    return DiagnosisKey.builder()
        .withKeyData(randomBytes)
        .withRollingStartIntervalNumber(rollingStartInterval)
        .withTransmissionRiskLevel(2)
        .withSubmissionTimestamp(submissionTimeStamp)
        .withCountryCode(countryCode)
        .withVisitedCountries(visitedCountries)
        .withReportType(reportType)
        .withConsentToFederation(consentToFederation)
        .build();
  }

  public static DiagnosisKey buildDiagnosisKeyForSubmissionTimestamp(long submissionTimeStamp) {
    return buildDiagnosisKeyForSubmissionTimestamp(submissionTimeStamp, false);
  }

  public static DiagnosisKey buildDiagnosisKeyForSubmissionTimestamp(long submissionTimeStamp, boolean consentToShare) {
    return buildDiagnosisKeyForSubmissionTimestamp(submissionTimeStamp, 600, consentToShare);
  }

  public static DiagnosisKey buildDiagnosisKeyForSubmissionTimestamp(long submissionTimeStamp,
      int rollingStartInterval, boolean consentToShare) {
    return buildDiagnosisKeyForSubmissionTimestamp(submissionTimeStamp, rollingStartInterval, consentToShare, "DE",
        Set.of("DE"), ReportType.CONFIRMED_TEST);
  }

  public static DiagnosisKey buildDiagnosisKeyForDateTime(OffsetDateTime dateTime) {
    return buildDiagnosisKeyForSubmissionTimestamp(dateTime.toEpochSecond() / 3600);
  }

  public static DiagnosisKey buildDiagnosisKeyForDateTime(OffsetDateTime dateTime,
      String countryCode, Set<String> visitedCountries, ReportType reportType) {
    return buildDiagnosisKeyForSubmissionTimestamp(dateTime.toEpochSecond() / 3600, 600, false, countryCode, visitedCountries, reportType);
  }

  /**
   * @return A key whos rolling period ended today (relative to test the test run) at 00:00,
   * but was submitted X hours after that time.
   * Altough the application uses minutes for expiration policies, the submission times are
   * computed relative to the top of the hours.
   */
  public static DiagnosisKey getKeySubmittedHoursAfterMidnightExpiration(int hours) {
    LocalDateTime yesterday = LocalDateTime.of(LocalDate.now(ZoneOffset.UTC), LocalTime.MIDNIGHT).minusDays(1);

    // key rolled out yesterday (relative to the test run) at 00:00
    int rollingStart = Math.toIntExact(yesterday.toEpochSecond(UTC) / 600L);
    LocalDateTime rollingPeriodExpiryTime = calculateRollingPeriodExpiryTime(rollingStart, 144);

    // submission time is minutes after rolling period has passed
    long submissionTime = rollingPeriodExpiryTime.plus(hours, ChronoUnit.HOURS)
            .toEpochSecond(UTC) / TimeUnit.HOURS.toSeconds(1);

    return buildDiagnosisKeyForSubmissionTimestamp(submissionTime, rollingStart, true);
  }

  /**
   * Returns the end of the rolling time window for the given rolling period and start interval numbers,
   * as a {@link LocalDateTime}.
   */
  private static LocalDateTime calculateRollingPeriodExpiryTime(long rollingStartInterval, int rollingPeriod) {
    return LocalDateTime
        .ofEpochSecond(rollingStartInterval * TEN_MINUTES_INTERVAL_SECONDS, 0, UTC)
        .plusMinutes(rollingPeriod * ROLLING_PERIOD_MINUTES_INTERVAL);
  }
}
