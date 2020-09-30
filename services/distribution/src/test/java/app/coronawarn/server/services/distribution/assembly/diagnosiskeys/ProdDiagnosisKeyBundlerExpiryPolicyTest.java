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

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import app.coronawarn.server.services.distribution.common.Helpers;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, KeySharingPoliciesChecker.class, ProdDiagnosisKeyBundler.class},
    initializers = ConfigFileApplicationContextInitializer.class)
class ProdDiagnosisKeyBundlerExpiryPolicyTest {

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  DiagnosisKeyBundler bundler;

  @ParameterizedTest
  @ValueSource(longs = {0L, 24L, 24L + 2L})
  void testLastPeriodOfHourAndSubmissionLessThanDistributionDateTime(long submissionTimestamp) {
    List<DiagnosisKey> diagnosisKeys = Helpers.buildDiagnosisKeys(5, submissionTimestamp, 10);
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 3, 0, 0), "DE")).hasSize(10);
  }

  @Test
  void testLastPeriodOfHourAndSubmissionEqualsDistributionDateTime() {
    List<DiagnosisKey> diagnosisKeys = Helpers.buildDiagnosisKeys(5, 24L + 3L, 10);
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 3, 0, 0), "DE")).hasSize(10);
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, 24L, 24L + 2L, 24L + 3L})
  void testFirstPeriodOfHourAndSubmissionLessThanDistributionDateTime(long submissionTimestamp) {
    List<DiagnosisKey> diagnosisKeys = Helpers.buildDiagnosisKeys(6, submissionTimestamp, 10);
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 4, 0, 0), "DE")).hasSize(10);
  }

  @Test
  void testFirstPeriodOfHourAndSubmissionEqualsDistributionDateTime() {
    List<DiagnosisKey> diagnosisKeys = Helpers.buildDiagnosisKeys(6, 24L + 4L, 10);
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 4, 0, 0), "DE")).hasSize(10);
  }

  @Test
  void testLastPeriodOfHourAndSubmissionGreaterDistributionDateTime() {
    List<DiagnosisKey> diagnosisKeys = Helpers.buildDiagnosisKeys(5, 24L + 4L, 10);
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 4, 0, 0), "DE")).hasSize(10);
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, 24L, 24L + 2L})
  void testLastPeriodOfHourAndSubmissionLessThanDistributionDateTimeWithFlexibleRollingPeriod(
      long submissionTimestamp) {
    List<DiagnosisKey> diagnosisKeys = Helpers.buildDiagnosisKeysWithFlexibleRollingPeriod(5, submissionTimestamp, 5, 44);
    diagnosisKeys.addAll(Helpers.buildDiagnosisKeysWithFlexibleRollingPeriod(5, submissionTimestamp, 5, 100));
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 3, 0, 0),"DE")).hasSize(10);
  }

  @Test
  void testLastPeriodOfHourAndSubmissionEqualsDistributionDateTimeWithFlexibleRollingPeriod() {
    List<DiagnosisKey> diagnosisKeys = Helpers.buildDiagnosisKeysWithFlexibleRollingPeriod(5, 24L + 3L, 5, 44);
    diagnosisKeys.addAll(Helpers.buildDiagnosisKeysWithFlexibleRollingPeriod(5, 24L + 3L, 5, 100));
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 3, 0, 0),"DE")).hasSize(10);
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, 24L, 24L + 2L, 24L + 3L})
  void testFirstPeriodOfHourAndSubmissionLessThanDistributionDateTimeWithFlexibleRollingPeriod(long submissionTimestamp) {
    List<DiagnosisKey> diagnosisKeys = Helpers.buildDiagnosisKeysWithFlexibleRollingPeriod(6, submissionTimestamp, 5, 44);
    diagnosisKeys.addAll(Helpers.buildDiagnosisKeysWithFlexibleRollingPeriod(6, submissionTimestamp, 5, 100));
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 4, 0, 0),"DE")).hasSize(10);
  }

  @Test
  void testFirstPeriodOfHourAndSubmissionEqualsDistributionDateTimeWithFlexibleRollingPeriod() {
    List<DiagnosisKey> diagnosisKeys = Helpers.buildDiagnosisKeysWithFlexibleRollingPeriod(6, 24L + 4L, 5, 44);
    diagnosisKeys.addAll(Helpers.buildDiagnosisKeysWithFlexibleRollingPeriod(6, 24L + 4L, 5, 100));
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 4, 0, 0),"DE")).hasSize(10);
  }

  @Test
  void testLastPeriodOfHourAndSubmissionGreaterDistributionDateTimeWithFlexibleRollingPeriod() {
    List<DiagnosisKey> diagnosisKeys = Helpers.buildDiagnosisKeysWithFlexibleRollingPeriod(5, 24L + 4L, 5, 44);
    diagnosisKeys.addAll(Helpers.buildDiagnosisKeysWithFlexibleRollingPeriod(5, 24L + 4L, 5, 80));
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 4, 0, 0),"DE")).hasSize(10);
  }
}
