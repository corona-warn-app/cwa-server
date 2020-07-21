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

package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.distribution.assembly.structure.util.TimeUtils;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.TestData;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;

import static app.coronawarn.server.services.distribution.common.Helpers.buildDiagnosisKeys;
import static org.mockito.Mockito.*;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, TestDataGeneration.class},
    initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles(profiles = "testdata")
class TestDataGenerationTest {

  @MockBean
  DiagnosisKeyService diagnosisKeyService;

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  TestDataGeneration testDataGeneration;

  @Captor
  private ArgumentCaptor<Collection<DiagnosisKey>> captor;

  @BeforeEach
  void setup() {
    var testData = new TestData();

    testData.setExposuresPerHour(2);
    testData.setSeed(0);
    distributionServiceConfig.setRetentionDays(1);
    distributionServiceConfig.setTestData(testData);
  }

  @Test
  void shouldCreateKeysAllKeys() {
    var now = LocalDateTime.of(2020, 7, 15, 12, 0, 0).toInstant(ZoneOffset.UTC);
    TimeUtils.setNow(now);

    when(diagnosisKeyService.getDiagnosisKeys()).thenReturn(Collections.emptyList());
    testDataGeneration.run(null);

    verify(diagnosisKeyService).saveDiagnosisKeys(captor.capture());
    Assert.assertFalse(captor.getValue().isEmpty());
  }

  @Test
  void shouldNotStoreAnyKeysInTheDatabase() {
    var now = LocalDateTime.of(2020, 7, 15, 12, 0, 0).toInstant(ZoneOffset.UTC);
    TimeUtils.setNow(now);

    when(diagnosisKeyService.getDiagnosisKeys())
        .thenReturn(buildDiagnosisKeys(6, LocalDateTime.of(2020, 7, 15, 12, 0, 0), 10));

    testDataGeneration.run(null);
    verify(diagnosisKeyService, never()).saveDiagnosisKeys(captor.capture());
  }

  @Test
  void shouldStoreOnlyKeysForLastHour() {
    var now = LocalDateTime.of(2020, 7, 15, 12, 0, 0).toInstant(ZoneOffset.UTC);
    TimeUtils.setNow(now);

    when(diagnosisKeyService.getDiagnosisKeys())
        .thenReturn(buildDiagnosisKeys(6, LocalDateTime.of(2020, 7, 15, 11, 0, 0), 10));

    testDataGeneration.run(null);
    verify(diagnosisKeyService, atMostOnce()).saveDiagnosisKeys(captor.capture());
    Assert.assertTrue(captor.getValue().stream()
        .allMatch(k -> k.getSubmissionTimestamp() != 443003));
  }
}
