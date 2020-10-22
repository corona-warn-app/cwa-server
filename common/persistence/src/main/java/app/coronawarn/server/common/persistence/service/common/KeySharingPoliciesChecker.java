

package app.coronawarn.server.common.persistence.service.common;


import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.springframework.stereotype.Component;


/**
 * Responsible for verifying that all policies enforced by DPP regulations (or stakeholders) are
 * met prior to sharing diagnosis keys with other external systems. Examples of such rules:
 *
 * <p><li> {@link ExpirationPolicy}
 */
@Component
public class KeySharingPoliciesChecker {

  /**
   * The submission timestamp is counted in 1 hour intervals since epoch.
   */
  public static final long ONE_HOUR_INTERVAL_SECONDS = TimeUnit.HOURS.toSeconds(1);

  /**
   * The rolling start interval number is counted in 10 minute intervals since epoch.
   */
  public static final long TEN_MINUTES_INTERVAL_SECONDS = TimeUnit.MINUTES
      .toSeconds(DiagnosisKey.ROLLING_PERIOD_MINUTES_INTERVAL);

  private static final Map<ChronoUnit, Function<Duration, Long>> TIME_CONVERTERS
       = Map.of(ChronoUnit.SECONDS, Duration::toSeconds,
                ChronoUnit.MINUTES, Duration::toMinutes,
                ChronoUnit.HOURS, Duration::toHours);

  /**
   * Returns true if the given diagnosis key can be shared at the given time taking into account
   * the expiration policy.
   */
  public boolean canShareKeyAtTime(DiagnosisKey key, ExpirationPolicy policy, LocalDateTime timeToShare) {
    LocalDateTime earliestTimeToShare = getEarliestTimeForSharingKey(key, policy);
    return timeToShare.isAfter(earliestTimeToShare) || timeToShare.isEqual(earliestTimeToShare);
  }

  /**
   * Calculates the earliest point in time at which the specified {@link DiagnosisKey} can be shared with external
   * systems, while respecting the expiry policy and the submission timestamp.
   *
   * @return {@link LocalDateTime} at which the specified {@link DiagnosisKey} can be shared.
   */
  public LocalDateTime getEarliestTimeForSharingKey(DiagnosisKey diagnosisKey, ExpirationPolicy policy) {
    LocalDateTime submissionDateTime = getSubmissionDateTime(diagnosisKey);
    LocalDateTime expiryDateTime = getRollingPeriodExpiryTime(diagnosisKey);
    long timeBetweenExpiryAndSubmission = TIME_CONVERTERS.get(policy.getTimeUnit())
        .apply(Duration.between(expiryDateTime, submissionDateTime));
    if (timeBetweenExpiryAndSubmission <= policy.getExpirationTime()) {
      // truncatedTo floors the value, so we need to add an hour to the DISTRIBUTION_PADDING to compensate that.
      return expiryDateTime.plusMinutes(policy.getExpirationTime() + 60).truncatedTo(ChronoUnit.HOURS);
    } else {
      return submissionDateTime;
    }
  }

  /**
   * Returns the end of the rolling time window that a {@link DiagnosisKey} was active for as a {@link LocalDateTime}.
   * The ".plusDays(1L)" is used as there can be now diagnosis keys with rollingPeriod set to less than 1 day.
   */
  private LocalDateTime getRollingPeriodExpiryTime(DiagnosisKey diagnosisKey) {
    return LocalDateTime
        .ofEpochSecond(diagnosisKey.getRollingStartIntervalNumber() * TEN_MINUTES_INTERVAL_SECONDS, 0, UTC)
        .plusMinutes(diagnosisKey.getRollingPeriod() * DiagnosisKey.ROLLING_PERIOD_MINUTES_INTERVAL);
  }

  /**
   * Returns the submission timestamp of a {@link DiagnosisKey} as a {@link LocalDateTime}.
   */
  private LocalDateTime getSubmissionDateTime(DiagnosisKey diagnosisKey) {
    return LocalDateTime.ofEpochSecond(diagnosisKey.getSubmissionTimestamp() * ONE_HOUR_INTERVAL_SECONDS, 0, UTC);
  }
}
