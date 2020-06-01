package app.coronawarn.server.services.distribution.assembly.diagnosiskeys;

import static app.coronawarn.server.services.distribution.assembly.diagnosiskeys.util.DateTime.ONE_HOUR_INTERVAL_SECONDS;
import static app.coronawarn.server.services.distribution.assembly.diagnosiskeys.util.DateTime.TEN_MINUTES_INTERVAL_SECONDS;
import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.groupingBy;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An instance of this class contains a collection of {@link DiagnosisKey DiagnosisKeys}.
 */
public class DiagnosisKeyBundler {

  private final DistributionServiceConfig distributionServiceConfig;

  // A map containing diagnosis keys, grouped by the LocalDateTime on which they may be distributed
  private final Map<LocalDateTime, List<DiagnosisKey>> distributableDiagnosisKeys;

  /**
   * Creates a new {@link DiagnosisKeyBundler}.
   *
   * @param diagnosisKeys A {@link List} of {@link DiagnosisKey DiagnosisKeys}.
   */
  public DiagnosisKeyBundler(Collection<DiagnosisKey> diagnosisKeys,
      DistributionServiceConfig distributionServiceConfig) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.distributableDiagnosisKeys = createDiagnosisKeyDistributionMap(diagnosisKeys);
  }

  private Map<LocalDateTime, List<DiagnosisKey>> createDiagnosisKeyDistributionMap(
      Collection<DiagnosisKey> diagnosisKeys) {
    if (distributionServiceConfig.getExpiryPolicyEnabled()) {
      return diagnosisKeys.stream().collect(groupingBy(this::getDistributionDateTime));
    } else {
      return diagnosisKeys.stream().collect(groupingBy(this::getSubmissionDateTime));
    }
  }

  /**
   * Returns all {@link DiagnosisKey DiagnosisKeys} contained by this {@link DiagnosisKeyBundler}.
   */
  public List<DiagnosisKey> getAllDiagnosisKeys() {
    return this.distributableDiagnosisKeys.values().stream()
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  /**
   * Returns all diagnosis keys that should be distributed in a specific hour.
   */
  public List<DiagnosisKey> getDiagnosisKeysDistributableAt(LocalDateTime hour) {
    if (distributionServiceConfig.getShiftingPolicyEnabled()) {
      List<DiagnosisKey> keysSinceLastDistribution = getKeysSinceLastDistribution(hour);
      if (keysSinceLastDistribution.size() >= distributionServiceConfig.getShiftingPolicyThreshold()) {
        return keysSinceLastDistribution;
      } else {
        return List.of();
      }
    } else {
      return getDiagnosisKeysForHour(hour);
    }
  }

  /**
   * Returns a all distributable keys between a specific hour and the last distribution (bundle that was above the
   * shifting threshold) or the earliest distributable key.
   */
  private List<DiagnosisKey> getKeysSinceLastDistribution(LocalDateTime hour) {
    Optional<LocalDateTime> earliestDistributableTimestamp = getEarliestDistributableTimestamp();
    if (earliestDistributableTimestamp.isEmpty()) {
      return List.of();
    }
    if (hour.isBefore(earliestDistributableTimestamp.get())) {
      return List.of();
    }
    List<DiagnosisKey> distributableInCurrentHour = getDiagnosisKeysForHour(hour);
    if (distributableInCurrentHour.size() >= distributionServiceConfig.getShiftingPolicyThreshold()) {
      return distributableInCurrentHour;
    }
    LocalDateTime previousHour = hour.minusHours(1);
    Collection<DiagnosisKey> distributableInPreviousHour = getDiagnosisKeysDistributableAt(previousHour);
    if (distributableInPreviousHour.size() >= distributionServiceConfig.getShiftingPolicyThreshold()) {
      // Last hour was distributed, so we can not combine the current hour with the last hour
      return distributableInCurrentHour;
    } else {
      // Last hour was not distributed, so we can combine the current hour with the last hour
      return Stream.concat(distributableInCurrentHour.stream(), getKeysSinceLastDistribution(previousHour).stream())
          .collect(Collectors.toList());
    }
  }

  private Optional<LocalDateTime> getEarliestDistributableTimestamp() {
    return this.distributableDiagnosisKeys.keySet().stream().min(LocalDateTime::compareTo);
  }

  /**
   * Returns the submission timestamp of a {@link DiagnosisKey} as a {@link LocalDateTime}.
   */
  private LocalDateTime getSubmissionDateTime(DiagnosisKey diagnosisKey) {
    return LocalDateTime.ofEpochSecond(diagnosisKey.getSubmissionTimestamp() * ONE_HOUR_INTERVAL_SECONDS, 0, UTC);
  }

  /**
   * Returns the end of the rolling time window that a {@link DiagnosisKey} was active for as a {@link LocalDateTime}.
   */
  private LocalDateTime getExpiryDateTime(DiagnosisKey diagnosisKey) {
    return LocalDateTime
        .ofEpochSecond(diagnosisKey.getRollingStartIntervalNumber() * TEN_MINUTES_INTERVAL_SECONDS, 0, UTC)
        .plusMinutes(diagnosisKey.getRollingPeriod() * 10L);
  }

  /**
   * Calculates the earliest point in time at which the specified {@link DiagnosisKey} can be distributed. Before keys
   * are allowed to be distributed, they must be expired for a configured amount of time.
   *
   * @return {@link LocalDateTime} at which the specified {@link DiagnosisKey} can be distributed.
   */
  private LocalDateTime getDistributionDateTime(DiagnosisKey diagnosisKey) {
    LocalDateTime submissionDateTime = getSubmissionDateTime(diagnosisKey);
    LocalDateTime expiryDateTime = getExpiryDateTime(diagnosisKey);
    long expiryPolicyMinutes = distributionServiceConfig.getExpiryPolicyMinutes();
    long minutesBetweenExpiryAndSubmission = Duration.between(expiryDateTime, submissionDateTime).toMinutes();
    if (minutesBetweenExpiryAndSubmission <= expiryPolicyMinutes) {
      // truncatedTo floors the value, so we need to add an hour to the DISTRIBUTION_PADDING to compensate that.
      return expiryDateTime.plusMinutes(expiryPolicyMinutes + 60).truncatedTo(ChronoUnit.HOURS);
    } else {
      return submissionDateTime;
    }
  }

  /**
   * Returns all diagnosis keys that should be distributed in a specific hour.
   */
  private List<DiagnosisKey> getDiagnosisKeysForHour(LocalDateTime hour) {
    return Optional
        .ofNullable(this.distributableDiagnosisKeys.get(hour))
        .orElse(List.of());
  }
}
