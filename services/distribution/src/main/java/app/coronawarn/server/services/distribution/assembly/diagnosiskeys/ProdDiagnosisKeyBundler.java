/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys;

import static app.coronawarn.server.services.distribution.assembly.diagnosiskeys.util.DateTime.TEN_MINUTES_INTERVAL_SECONDS;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.context.annotation.Profile;

/**
 * An instance of this class contains a collection of {@link DiagnosisKey DiagnosisKeys}, that will be distributed while
 * respecting expiry policy (keys must be expired for a configurable amount of time before distribution) and shifting
 * policy (there must be at least a configurable number of keys in a distribution). The policies are configurable
 * through the properties {@code expiry-policy-minutes} and {@code shifting-policy-threshold}.
 */
@Profile("!demo")
public class ProdDiagnosisKeyBundler extends DiagnosisKeyBundler {

  /**
   * Creates a new {@link ProdDiagnosisKeyBundler}.
   */
  public ProdDiagnosisKeyBundler(DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig);
  }

  /**
   * Initializes the internal {@code distributableDiagnosisKeys} map, grouping the diagnosis keys by the date on which
   * they may be distributed, while respecting the expiry policy.
   */
  @Override
  protected void createDiagnosisKeyDistributionMap(Collection<DiagnosisKey> diagnosisKeys) {
    this.distributableDiagnosisKeys.clear();
    this.distributableDiagnosisKeys.putAll(diagnosisKeys.stream().collect(groupingBy(this::getDistributionDateTime)));
  }

  /**
   * Returns all diagnosis keys that should be distributed in a specific hour, while respecting the shifting and expiry
   * policies.
   */
  @Override
  public List<DiagnosisKey> getDiagnosisKeysDistributableAt(LocalDateTime hour) {
    List<DiagnosisKey> keysSinceLastDistribution = getKeysSinceLastDistribution(hour);
    if (keysSinceLastDistribution.size() >= minNumberOfKeysPerBundle) {
      return keysSinceLastDistribution;
    } else {
      return emptyList();
    }
  }

  /**
   * Returns a all distributable keys between a specific hour and the last distribution (bundle that was above the
   * shifting threshold) or the earliest distributable key.
   */
  private List<DiagnosisKey> getKeysSinceLastDistribution(LocalDateTime hour) {
    Optional<LocalDateTime> earliestDistributableTimestamp = getEarliestDistributableTimestamp();
    if (earliestDistributableTimestamp.isEmpty() || hour.isBefore(earliestDistributableTimestamp.get())) {
      return emptyList();
    }
    List<DiagnosisKey> distributableInCurrentHour = getDiagnosisKeysForHour(hour);
    if (distributableInCurrentHour.size() >= minNumberOfKeysPerBundle) {
      return distributableInCurrentHour;
    }
    LocalDateTime previousHour = hour.minusHours(1);
    Collection<DiagnosisKey> distributableInPreviousHour = getDiagnosisKeysDistributableAt(previousHour);
    if (distributableInPreviousHour.size() >= minNumberOfKeysPerBundle) {
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
    long minutesBetweenExpiryAndSubmission = Duration.between(expiryDateTime, submissionDateTime).toMinutes();
    if (minutesBetweenExpiryAndSubmission <= expiryPolicyMinutes) {
      // truncatedTo floors the value, so we need to add an hour to the DISTRIBUTION_PADDING to compensate that.
      return expiryDateTime.plusMinutes(expiryPolicyMinutes + 60).truncatedTo(ChronoUnit.HOURS);
    } else {
      return submissionDateTime;
    }
  }
}
