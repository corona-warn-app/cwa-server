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
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

  /**
   * Creates a new {@link ProdDiagnosisKeyBundler}.
   */
  public ProdDiagnosisKeyBundler(DistributionServiceConfig distributionServiceConfig,
      KeySharingPoliciesChecker sharingPoliciesChecker) {
    super(distributionServiceConfig);
    this.sharingPoliciesChecker = sharingPoliciesChecker;
  }

  /**
   * Initializes the internal {@code distributableDiagnosisKeys} map, grouping the diagnosis keys by the date on which
   * they may be distributed, while respecting the expiry and shifting policies.
   */
  @Override
  protected void createDiagnosisKeyDistributionMap(Collection<DiagnosisKey> diagnosisKeys) {
    this.distributableDiagnosisKeys.clear();
    if (diagnosisKeys.isEmpty()) {
      return;
    }
    Map<LocalDateTime, List<DiagnosisKey>> distributableDiagnosisKeysGroupedByExpiryPolicy = new HashMap<>(
        diagnosisKeys.stream().collect(groupingBy(this::getDistributionDateTimeByExpiryPolicy)));
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
            this.distributableDiagnosisKeys.put(currentHour, new ArrayList<>(diagnosisKeyAccumulator));
            diagnosisKeyAccumulator.clear();
          } else {
            // placeholder list is needed to be able to generate empty file - see issue #650
            this.distributableDiagnosisKeys.put(currentHour, Collections.emptyList());
          }
        });
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
