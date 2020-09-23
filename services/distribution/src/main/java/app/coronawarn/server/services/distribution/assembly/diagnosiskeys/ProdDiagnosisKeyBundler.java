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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.common.ExpirationPolicy;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.LongStream;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * An instance of this class contains a collection of {@link DiagnosisKey DiagnosisKeys}, that will be distributed while
 * respecting expiry policy (keys must be expired for a configurable amount of time before distribution) and shifting
 * policy (there must be at least a configurable number of keys in a distribution). The policies are configurable
 * through the properties {@code expiry-policy-minutes} and {@code shifting-policy-threshold}.
 */
@Profile("!demo")
@Component
public class ProdDiagnosisKeyBundler extends DiagnosisKeyBundler {

  private KeySharingPoliciesChecker sharingPoliciesChecker;
  private String originCountry;
  private String euPackageName;
  private boolean applyPoliciesForAllCountries;

  /**
   * Creates a new {@link ProdDiagnosisKeyBundler}.
   */
  public ProdDiagnosisKeyBundler(DistributionServiceConfig distributionServiceConfig,
      KeySharingPoliciesChecker sharingPoliciesChecker) {
    super(distributionServiceConfig);
    this.sharingPoliciesChecker = sharingPoliciesChecker;
    this.originCountry = distributionServiceConfig.getApi().getOriginCountry();
    this.euPackageName = distributionServiceConfig.getEuPackageName();
    this.applyPoliciesForAllCountries = distributionServiceConfig.getApplyPoliciesForAllCountries();
  }

  /**
   * Initializes the internal {@code distributableDiagnosisKeys} map, grouping the diagnosis keys based on the country
   * and by the date on which they may be distributed, while respecting the expiry and shifting policies.
   */
  @Override
  protected void createDiagnosisKeyDistributionMap(Collection<DiagnosisKey> diagnosisKeys) {
    this.distributableDiagnosisKeys.clear();
    Map<String, List<DiagnosisKey>> diagnosisKeysMapped = groupDiagnosisKeysByCountry(diagnosisKeys);

    diagnosisKeysMapped.keySet().forEach(country -> {
      if (!country.equals(originCountry) && !applyPoliciesForAllCountries) {
        populateDistributableDiagnosisKeysWithoutPolicies(diagnosisKeysMapped, country);
      } else {
        populateDistributableDiagnosisKeysWithPolicies(diagnosisKeysMapped, country);
      }
    });
    populateEuPackageWithDistributableDiagnosisKeys();
  }

  private void populateEuPackageWithDistributableDiagnosisKeys() {
    Map<LocalDateTime, Set<DiagnosisKey>> euPackage = new HashMap<>();

    distributableDiagnosisKeys
        .forEach((country, diagnosisKeyMap) -> diagnosisKeyMap.forEach((distributionDateTime, diagnosisKeys) -> {
          Set<DiagnosisKey> currentHourDiagnosisKeys = Optional
              .ofNullable(euPackage.get(distributionDateTime))
              .orElse(new HashSet<>());
          currentHourDiagnosisKeys.addAll(diagnosisKeys);
          euPackage.put(distributionDateTime, currentHourDiagnosisKeys);
        }));

    Map<LocalDateTime, List<DiagnosisKey>> euPackageList = new HashMap<>();
    euPackage.forEach((distributionDateTime, diagnosisKeys) -> {
      euPackageList.put(distributionDateTime, new ArrayList<>(diagnosisKeys));
    });
    distributableDiagnosisKeys.put(euPackageName, euPackageList);
  }

  private void populateDistributableDiagnosisKeysWithPolicies(Map<String, List<DiagnosisKey>> diagnosisKeysMapped,
      String country) {

    Map<LocalDateTime, List<DiagnosisKey>> distributableDiagnosisKeysGroupedByExpiryPolicy = new HashMap<>(
        diagnosisKeysMapped.get(country).stream().collect(groupingBy(this::getDistributionDateTimeByExpiryPolicy)));

    if (distributableDiagnosisKeysGroupedByExpiryPolicy.isEmpty()) {
      return;
    }

    LocalDateTime earliestDistributableTimestamp =
        getEarliestDistributableTimestamp(distributableDiagnosisKeysGroupedByExpiryPolicy).orElseThrow();
    LocalDateTime latestDistributableTimestamp = this.distributionTime;

    List<DiagnosisKey> diagnosisKeyAccumulator = new ArrayList<>();
    LongStream.range(0, earliestDistributableTimestamp.until(latestDistributableTimestamp, ChronoUnit.HOURS))
        .forEach(hourCounter -> {
          LocalDateTime currentHour = earliestDistributableTimestamp.plusHours(hourCounter);
          Collection<DiagnosisKey> currentHourDiagnosisKeys = Optional
              .ofNullable(distributableDiagnosisKeysGroupedByExpiryPolicy.get(currentHour))
              .orElse(emptyList());
          diagnosisKeyAccumulator.addAll(currentHourDiagnosisKeys);
          if (diagnosisKeyAccumulator.size() >= minNumberOfKeysPerBundle) {
            this.distributableDiagnosisKeys.get(country).put(currentHour, new ArrayList<>(diagnosisKeyAccumulator));
            diagnosisKeyAccumulator.clear();
          } else {
            // placeholder list is needed to be able to generate empty file - see issue #650
            this.distributableDiagnosisKeys.get(country).put(currentHour, Collections.emptyList());
          }
        });
  }

  private void populateDistributableDiagnosisKeysWithoutPolicies(Map<String, List<DiagnosisKey>> diagnosisKeysMapped,
      String country) {

    this.distributableDiagnosisKeys.get(country).putAll(diagnosisKeysMapped.get(country).stream()
        .filter(diagnosisKey -> this.getSubmissionDateTime(diagnosisKey).isBefore(this.distributionTime))
        .collect(groupingBy(this::getSubmissionDateTime)));
  }

  private static Optional<LocalDateTime> getEarliestDistributableTimestamp(
      Map<LocalDateTime, List<DiagnosisKey>> distributableDiagnosisKeys) {
    return distributableDiagnosisKeys.keySet().stream().min(LocalDateTime::compareTo);
  }

  private LocalDateTime getDistributionDateTimeByExpiryPolicy(DiagnosisKey diagnosisKey) {
    return sharingPoliciesChecker.getEarliestTimeForSharingKey(diagnosisKey,
        ExpirationPolicy.of(expiryPolicyMinutes, ChronoUnit.MINUTES));
  }
}
