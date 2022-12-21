package app.coronawarn.server.common.persistence.service;

import static app.coronawarn.server.common.persistence.domain.DiagnosisKey.ROLLING_PERIOD_MINUTES_INTERVAL;
import static app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker.TEN_MINUTES_INTERVAL_SECONDS;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DiagnosisKeyServiceTestHelper {

  private static final Random random = new Random();

  public static void assertDiagnosisKeysEqual(Collection<DiagnosisKey> expKeys, Collection<DiagnosisKey> actKeys) {
    assertThat(actKeys).withFailMessage("Cardinality mismatch").hasSameSizeAs(expKeys);

    final Iterator<DiagnosisKey> expIt = expKeys.iterator();
    final Iterator<DiagnosisKey> actIt = actKeys.iterator();
    for (; expIt.hasNext() && actIt.hasNext();) {
      assertDiagnosisKeysEqual(expIt.next(), actIt.next());
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
    return DiagnosisKey.builder()
        .withKeyDataAndSubmissionType(randomByteData(), SubmissionType.SUBMISSION_TYPE_PCR_TEST)
        .withRollingStartIntervalNumber(rollingStartInterval)
        .withTransmissionRiskLevel(2)
        .withSubmissionTimestamp(submissionTimeStamp)
        .withCountryCode(countryCode)
        .withVisitedCountries(visitedCountries)
        .withReportType(reportType)
        .withConsentToFederation(consentToFederation)
        .build();
  }

  public static int makeRollingStartIntervalFromSubmission(long submissionTimestamp) {
    return (int) ((submissionTimestamp) * 6);
  }

  public static DiagnosisKey buildDiagnosisKeyForSubmissionTimestamp(long submissionTimeStamp) {
    return buildDiagnosisKeyForSubmissionTimestamp(submissionTimeStamp, false);
  }

  public static DiagnosisKey buildDiagnosisKeyForSubmissionTimestamp(long submissionTimeStamp, boolean consentToShare) {
    return buildDiagnosisKeyForSubmissionTimestamp(submissionTimeStamp,
        makeRollingStartIntervalFromSubmission(submissionTimeStamp), consentToShare);
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
    var submissionTimeStamp = dateTime.toEpochSecond() / 3600;
    return buildDiagnosisKeyForSubmissionTimestamp(submissionTimeStamp,
        makeRollingStartIntervalFromSubmission(submissionTimeStamp),
        false, countryCode, visitedCountries, reportType);
  }

  /**
   * @param hours number of hours
   * @return A key which's rolling period ended today (relative to test the test run) at 00:00, but was submitted X hours
   * after that time. Although the application uses minutes for expiration policies, the submission times are computed
   * relative to the top of the hours.
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
   * Returns the end of the rolling time window for the given rolling period and start interval numbers, as a {@link
   * LocalDateTime}.
   */
  private static LocalDateTime calculateRollingPeriodExpiryTime(long rollingStartInterval, int rollingPeriod) {
    return LocalDateTime
        .ofEpochSecond(rollingStartInterval * TEN_MINUTES_INTERVAL_SECONDS, 0, UTC)
        .plusMinutes(rollingPeriod * ROLLING_PERIOD_MINUTES_INTERVAL);
  }

  public static DiagnosisKey generateRandomDiagnosisKey(boolean consentToShare, long submissionTimestamp,
      SubmissionType submissionType) {
    return DiagnosisKey.builder()
        .withKeyDataAndSubmissionType(randomByteData(), submissionType)
        .withRollingStartIntervalNumber((int) submissionTimestamp * 6)
        .withTransmissionRiskLevel(2)
        .withConsentToFederation(consentToShare)
        .withCountryCode("DE")
        .withDaysSinceOnsetOfSymptoms(random.nextInt(13))
        .withSubmissionTimestamp(submissionTimestamp)
        .withVisitedCountries(Set.of("FR", "DK"))
        .withReportType(ReportType.CONFIRMED_TEST)
        .build();
  }

  public static DiagnosisKey generateRandomDiagnosisKeyWithSpecifiedTrl(boolean consentToShare, long submissionTimestamp,
      SubmissionType submissionType, int transmissionRiskLevel) {
    return DiagnosisKey.builder()
        .withKeyDataAndSubmissionType(randomByteData(), submissionType)
        .withRollingStartIntervalNumber((int) submissionTimestamp * 6)
        .withTransmissionRiskLevel(transmissionRiskLevel)
        .withConsentToFederation(consentToShare)
        .withCountryCode("DE")
        .withDaysSinceOnsetOfSymptoms(random.nextInt(13))
        .withSubmissionTimestamp(submissionTimestamp)
        .withVisitedCountries(Set.of("FR", "DK"))
        .withReportType(ReportType.CONFIRMED_TEST)
        .build();
  }

  private static byte[] randomByteData() {
    byte[] keyData = new byte[16];
    DiagnosisKeyServiceTestHelper.random.nextBytes(keyData);
    return keyData;
  }
}
