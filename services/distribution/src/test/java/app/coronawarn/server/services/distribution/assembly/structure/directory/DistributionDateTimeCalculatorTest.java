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

package app.coronawarn.server.services.distribution.assembly.structure.directory;

import static app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.DistributionDateTimeCalculator.getDistributionDateTime;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DistributionDateTimeCalculatorTest {

  @ParameterizedTest
  @ValueSource(longs = {0L, 24L, 24L + 2L})
  void testLastPeriodOfHourAndSubmissionLessThanDistributionDateTime(long submissionTimestamp) {
    var diagnosisKey = buildDiagnosisKey(5, submissionTimestamp);
    assertThat(getDistributionDateTime(diagnosisKey)).isEqualTo("1970-01-02T03:00");
  }

  @Test
  void testLastPeriodOfHourAndSubmissionEqualsDistributionDateTime() {
    var diagnosisKey = buildDiagnosisKey(5, 24L + 3L);
    assertThat(getDistributionDateTime(diagnosisKey)).isEqualTo("1970-01-02T03:00");
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, 24L, 24L + 2L, 24L + 3L})
  void testFirstPeriodOfHourAndSubmissionLessThanDistributionDateTime(long submissionTimestamp) {
    var diagnosisKey = buildDiagnosisKey(6, submissionTimestamp);
    assertThat(getDistributionDateTime(diagnosisKey)).isEqualTo("1970-01-02T04:00");
  }

  @Test
  void testFirstPeriodOfHourAndSubmissionEqualsDistributionDateTime() {
    var diagnosisKey = buildDiagnosisKey(6, 24L + 4L);
    assertThat(getDistributionDateTime(diagnosisKey)).isEqualTo("1970-01-02T04:00");
  }

  @Test
  void testLastPeriodOfHourAndSubmissionGreaterDistributionDateTime() {
    var diagnosisKey = buildDiagnosisKey(5, 24L + 4L);
    assertThat(getDistributionDateTime(diagnosisKey)).isEqualTo("1970-01-02T04:00");
  }

  private DiagnosisKey buildDiagnosisKey(int startIntervalNumber, long submissionTimestamp) {
    return DiagnosisKey.builder()
        .withKeyData(new byte[16])
        .withRollingStartIntervalNumber(startIntervalNumber)
        .withTransmissionRiskLevel(2)
        .withSubmissionTimestamp(submissionTimestamp).build();
  }
}
