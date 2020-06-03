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

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.emptyList;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * An instance of this class contains a collection of {@link DiagnosisKey DiagnosisKeys}.
 */
public abstract class DiagnosisKeyBundler {

  /**
   * The submission timestamp is counted in 1 hour intervals since epoch.
   */
  public static final long ONE_HOUR_INTERVAL_SECONDS = TimeUnit.HOURS.toSeconds(1);

  /**
   * The rolling start interval number is counted in 10 minute intervals since epoch.
   */
  public static final long TEN_MINUTES_INTERVAL_SECONDS = TimeUnit.MINUTES.toSeconds(10);

  protected final int minNumberOfKeysPerBundle;
  protected final long expiryPolicyMinutes;

  // The hour at which the distribution runs. This field is needed to prevent the run from distributing any keys that
  // have already been submitted but may only be distributed in the future (e.g. because they are not expired yet).
  protected LocalDateTime distributionTime;

  // A map containing diagnosis keys, grouped by the LocalDateTime on which they may be distributed
  protected final Map<LocalDateTime, List<DiagnosisKey>> distributableDiagnosisKeys = new HashMap<>();

  public DiagnosisKeyBundler(DistributionServiceConfig distributionServiceConfig) {
    this.minNumberOfKeysPerBundle = distributionServiceConfig.getShiftingPolicyThreshold();
    this.expiryPolicyMinutes = distributionServiceConfig.getExpiryPolicyMinutes();
  }

  /**
   * Creates a {@link LocalDateTime} based on the specified epoch timestamp.
   */
  public static LocalDateTime getLocalDateTimeFromHoursSinceEpoch(long timestamp) {
    return LocalDateTime.ofEpochSecond(TimeUnit.HOURS.toSeconds(timestamp), 0, UTC);
  }

  /**
   * Sets the {@link DiagnosisKey DiagnosisKeys} contained by this {@link DiagnosisKeyBundler} and the time at which the
   * distribution runs and calls {@link DiagnosisKeyBundler#createDiagnosisKeyDistributionMap}.
   *
   * @param diagnosisKeys    The {@link DiagnosisKey DiagnosisKeys} contained by this {@link DiagnosisKeyBundler}.
   * @param distributionTime The {@link LocalDateTime} at which the distribution runs.
   */
  public void setDiagnosisKeys(Collection<DiagnosisKey> diagnosisKeys, LocalDateTime distributionTime) {
    this.distributionTime = distributionTime;
    this.createDiagnosisKeyDistributionMap(diagnosisKeys);
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
   * Initializes the internal {@code distributableDiagnosisKeys} map, which should contain all diagnosis keys, grouped
   * by the LocalDateTime on which they may be distributed.
   */
  protected abstract void createDiagnosisKeyDistributionMap(Collection<DiagnosisKey> diagnosisKeys);

  /**
   * Returns a set of all {@link LocalDate dates} on which {@link DiagnosisKey diagnosis keys} shall be distributed.
   */
  public Set<LocalDate> getDatesWithDistributableDiagnosisKeys() {
    return this.distributableDiagnosisKeys.keySet().stream()
        .map(LocalDateTime::toLocalDate)
        .collect(Collectors.toSet());
  }

  /**
   * Returns a set of all {@link LocalDateTime hours} of a specified {@link LocalDate date} during which {@link
   * DiagnosisKey diagnosis keys} shall be distributed.
   */
  public Set<LocalDateTime> getHoursWithDistributableDiagnosisKeys(LocalDate currentDate) {
    return this.distributableDiagnosisKeys.keySet().stream()
        .filter(dateTime -> dateTime.toLocalDate().equals(currentDate))
        .collect(Collectors.toSet());
  }

  /**
   * Returns the submission timestamp of a {@link DiagnosisKey} as a {@link LocalDateTime}.
   */
  protected LocalDateTime getSubmissionDateTime(DiagnosisKey diagnosisKey) {
    return LocalDateTime.ofEpochSecond(diagnosisKey.getSubmissionTimestamp() * ONE_HOUR_INTERVAL_SECONDS, 0, UTC);
  }

  /**
   * Returns all diagnosis keys that should be distributed on a specific date.
   */
  public List<DiagnosisKey> getDiagnosisKeysForDate(LocalDate date) {
    return this.distributableDiagnosisKeys.keySet().stream()
        .filter(dateTime -> dateTime.toLocalDate().equals(date))
        .map(this::getDiagnosisKeysForHour)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  /**
   * Returns all diagnosis keys that should be distributed in a specific hour.
   */
  public List<DiagnosisKey> getDiagnosisKeysForHour(LocalDateTime hour) {
    return Optional
        .ofNullable(this.distributableDiagnosisKeys.get(hour))
        .orElse(emptyList());
  }
}
