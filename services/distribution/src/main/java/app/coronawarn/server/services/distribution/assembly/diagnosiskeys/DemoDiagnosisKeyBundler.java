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

import static java.util.stream.Collectors.groupingBy;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.Collection;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * An instance of this class contains a collection of {@link DiagnosisKey DiagnosisKeys}, that will be distributed in
 * the same hour they have been submitted.
 */
@Profile("demo")
@Component
public class DemoDiagnosisKeyBundler extends DiagnosisKeyBundler {

  public DemoDiagnosisKeyBundler(DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig);
  }

  /**
   * Initializes the internal {@code distributableDiagnosisKeys} map, grouping the diagnosis keys by the submission
   * timestamp, thus ignoring the expiry and shifting policies.
   */
  @Override
  protected void createDiagnosisKeyDistributionMap(Collection<DiagnosisKey> diagnosisKeys) {
    this.distributableDiagnosisKeys.clear();
    this.distributableDiagnosisKeys.putAll(diagnosisKeys.stream().collect(groupingBy(this::getSubmissionDateTime)));
  }
}
