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

import static app.coronawarn.server.services.distribution.assembly.diagnosiskeys.util.DateTime.ONE_HOUR_INTERVAL_SECONDS;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.emptyList;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An instance of this class contains a collection of {@link DiagnosisKey DiagnosisKeys}.
 */
public abstract class DiagnosisKeyBundler {

  protected final int minNumberOfKeysPerBundle;
  protected final long expiryPolicyMinutes;

  // A map containing diagnosis keys, grouped by the LocalDateTime on which they may be distributed
  protected final Map<LocalDateTime, List<DiagnosisKey>> distributableDiagnosisKeys = new HashMap<>();

  public DiagnosisKeyBundler(DistributionServiceConfig distributionServiceConfig) {
    this.minNumberOfKeysPerBundle = distributionServiceConfig.getShiftingPolicyThreshold();
    this.expiryPolicyMinutes = distributionServiceConfig.getExpiryPolicyMinutes();
  }

  /**
   * Sets the {@link DiagnosisKey DiagnosisKeys} contained by this {@link DiagnosisKeyBundler} and calls {@link
   * DiagnosisKeyBundler#createDiagnosisKeyDistributionMap}.
   */
  public void setDiagnosisKeys(Collection<DiagnosisKey> diagnosisKeys) {
    createDiagnosisKeyDistributionMap(diagnosisKeys);
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
   * Returns all diagnosis keys that should be distributed in a specific hour.
   */
  public abstract List<DiagnosisKey> getDiagnosisKeysDistributableAt(LocalDateTime hour);

  /**
   * Returns the submission timestamp of a {@link DiagnosisKey} as a {@link LocalDateTime}.
   */
  protected LocalDateTime getSubmissionDateTime(DiagnosisKey diagnosisKey) {
    return LocalDateTime.ofEpochSecond(diagnosisKey.getSubmissionTimestamp() * ONE_HOUR_INTERVAL_SECONDS, 0, UTC);
  }

  /**
   * Returns all diagnosis keys that should be distributed in a specific hour.
   */
  protected List<DiagnosisKey> getDiagnosisKeysForHour(LocalDateTime hour) {
    return Optional
        .ofNullable(this.distributableDiagnosisKeys.get(hour))
        .orElse(emptyList());
  }
}
