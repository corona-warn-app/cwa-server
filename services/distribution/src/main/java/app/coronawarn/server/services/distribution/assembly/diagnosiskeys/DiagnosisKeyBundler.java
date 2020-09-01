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
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.core.Local;

/**
 * An instance of this class contains a collection of {@link DiagnosisKey DiagnosisKeys}.
 */
public abstract class DiagnosisKeyBundler {

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyBundler.class);

  /**
   * The submission timestamp is counted in 1 hour intervals since epoch.
   */
  public static final long ONE_HOUR_INTERVAL_SECONDS = TimeUnit.HOURS.toSeconds(1);

  /**
   * The rolling start interval number is counted in 10 minute intervals since epoch.
   */
  public static final long TEN_MINUTES_INTERVAL_SECONDS = TimeUnit.MINUTES.toSeconds(10);

  protected final long expiryPolicyMinutes;
  protected final int minNumberOfKeysPerBundle;
  private final int maxNumberOfKeysPerBundle;
  protected final List<String> supportedCountries;
  /**
   * The hour at which the distribution runs. This field is needed to prevent the run from distributing any keys that
   * have already been submitted but may only be distributed in the future (e.g. because they are not expired yet).
   */
  protected LocalDateTime distributionTime;

  /**
   * A map containing diagnosis keys, grouped by country and mapped by the LocalDateTime
   * on which they may be distributed.
   */
  protected final Map<String, Map<LocalDateTime, List<DiagnosisKey>>> distributableDiagnosisKeys = new HashMap<>();

  /**
   * Constructs a DiagnosisKeyBundler based on the specified service configuration.
   */
  public DiagnosisKeyBundler(DistributionServiceConfig distributionServiceConfig) {
    this.supportedCountries = List.of(distributionServiceConfig.getSupportedCountries());
    this.expiryPolicyMinutes = distributionServiceConfig.getExpiryPolicyMinutes();
    this.minNumberOfKeysPerBundle = distributionServiceConfig.getShiftingPolicyThreshold();
    this.maxNumberOfKeysPerBundle = distributionServiceConfig.getMaximumNumberOfKeysPerBundle();
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
   * Returns the {@link LocalDateTime} at which the distribution runs.
   */
  public LocalDateTime getDistributionTime() {
    return this.distributionTime;
  }

  /**
   * Returns all {@link DiagnosisKey DiagnosisKeys} contained by this {@link DiagnosisKeyBundler}.
   */
  public List<DiagnosisKey> getAllDiagnosisKeys(String country) {
    if (!supportedCountries.contains(country)) {
      throw new InvalidCountryException(
          String.join("The country {} received is not included in the list of supported countries", country));
    }
    return this.distributableDiagnosisKeys.get(country).values()
        .stream().flatMap(Collection::stream).collect(Collectors.toList());
  }

  /**
   * Initializes the internal {@code distributableDiagnosisKeys} map, which should contain all diagnosis keys, grouped
   * by the LocalDateTime on which they may be distributed.
   */
  protected abstract void createDiagnosisKeyDistributionMap(Collection<DiagnosisKey> diagnosisKeys);

  /**
   * Returns a set of all {@link LocalDate dates} on which {@link DiagnosisKey diagnosis keys} shall be distributed
   * based on country.
   */
  public Set<LocalDate> getDatesWithDistributableDiagnosisKeys(String country) {
    if (!supportedCountries.contains(country)) {
      throw new InvalidCountryException(
          String.join("The country {} received is not included in the list of supported countries", country));
    }
    return this.distributableDiagnosisKeys.get(country).keySet().stream()
        .map(LocalDateTime::toLocalDate)
        .filter(date -> numberOfKeysForDateBelowMaximum(date, country))
        .collect(Collectors.toSet());
  }

  public boolean numberOfKeysForDateBelowMaximum(LocalDate date, String country) {
    return numberOfKeysBelowMaximum(getDiagnosisKeysForDate(date, country).size(), date);
  }

  /**
   * Returns a map of all {@link LocalDateTime hours} of a specified {@link LocalDate date} and country during which
   * {@link DiagnosisKey diagnosis keys} shall be distributed.
   */
  public Set<LocalDateTime> getHoursWithDistributableDiagnosisKeys(LocalDate currentDate, String country) {
    return this.distributableDiagnosisKeys.get(country).keySet().stream()
        .filter(dateTime -> dateTime.toLocalDate().equals(currentDate))
        .filter(dateTime -> numberOfKeysForHourBelowMaximum(dateTime, country))
        .collect(Collectors.toSet());
  }

  private boolean numberOfKeysForHourBelowMaximum(LocalDateTime hour, String country) {
    return numberOfKeysBelowMaximum(getDiagnosisKeysForHour(hour, country).size(), hour);
  }

  private boolean numberOfKeysBelowMaximum(int numberOfKeys, Temporal time) {
    if (numberOfKeys > maxNumberOfKeysPerBundle) {
      logger.error("Number of diagnosis keys ({}) for {} exceeds the configured maximum.", numberOfKeys, time);
      return false;
    } else {
      return true;
    }
  }

  /**
   * Returns the submission timestamp of a {@link DiagnosisKey} as a {@link LocalDateTime}.
   */
  protected LocalDateTime getSubmissionDateTime(DiagnosisKey diagnosisKey) {
    return LocalDateTime.ofEpochSecond(diagnosisKey.getSubmissionTimestamp() * ONE_HOUR_INTERVAL_SECONDS, 0, UTC);
  }

  /**
   * Returns all diagnosis keys that should be distributed on a specific date for a specific country.
   */
  public List<DiagnosisKey> getDiagnosisKeysForDate(LocalDate date, String country) {
    if (!supportedCountries.contains(country)) {
      throw new InvalidCountryException(
          String.join("The country {} received is not included in the list of supported countries", country));
    }
    return this.distributableDiagnosisKeys.get(country).keySet().stream()
        .filter(dateTime -> dateTime.toLocalDate().equals(date))
        .map(dateTime -> getDiagnosisKeysForHour(dateTime, country))
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  /**
   * Returns all diagnosis keys that should be distributed in a specific hour for a specific country.
   */
  public List<DiagnosisKey> getDiagnosisKeysForHour(LocalDateTime hour, String country) {
    if (!supportedCountries.contains(country)) {
      throw new InvalidCountryException(
          String.join("The country {} received is not included in the list of supported countries", country));
    }
    return Optional
        .ofNullable(this.distributableDiagnosisKeys.get(country).get(hour))
        .orElse(emptyList());
  }

  protected Map<String, List<DiagnosisKey>> groupDiagnosisKeysByCountry(Collection<DiagnosisKey> diagnosisKeys) {
    Map<String, List<DiagnosisKey>> diagnosisKeysMapped = new HashMap<>();

    supportedCountries.forEach(supportedCountry -> {
      diagnosisKeysMapped.put(supportedCountry, new ArrayList<>());
    });

    diagnosisKeys.forEach(diagnosisKey -> diagnosisKey.getVisitedCountries().stream()
        .filter(supportedCountries::contains)
        .forEach(visitedCountry -> diagnosisKeysMapped.get(visitedCountry).add(diagnosisKey)));
    return diagnosisKeysMapped;
  }
}
