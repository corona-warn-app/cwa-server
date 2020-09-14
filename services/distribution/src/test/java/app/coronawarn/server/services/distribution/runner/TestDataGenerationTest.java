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

import static app.coronawarn.server.services.distribution.common.Helpers.buildDiagnosisKeys;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.distribution.assembly.structure.util.TimeUtils;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.TestData;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
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
    testDataGeneration = new TestDataGeneration(diagnosisKeyService, distributionServiceConfig);
  }

  @AfterEach
  void tearDown() {
    TimeUtils.setNow(null);
  }

  @Test
  void shouldCreateKeysAllKeys() {
    var now = LocalDateTime.of(2020, 7, 15, 12, 0, 0).toInstant(ZoneOffset.UTC);
    TimeUtils.setNow(now);

    when(diagnosisKeyService.getDiagnosisKeys()).thenReturn(Collections.emptyList());
    testDataGeneration.run(null);

    verify(diagnosisKeyService, times(distributionServiceConfig.getSupportedCountries().length))
        .saveDiagnosisKeys(captor.capture());
    Assert.assertFalse(captor.getValue().isEmpty());
  }

  @Test
  void shouldNotStoreAnyKeysInTheDatabase() {
    var now = LocalDateTime.of(2020, 7, 15, 12, 0, 0).toInstant(ZoneOffset.UTC);
    TimeUtils.setNow(now);

    when(diagnosisKeyService.getDiagnosisKeys())
        .thenReturn(buildDiagnosisKeys(6,
            LocalDateTime.of(2020, 7, 15, 12, 0, 0), 10, "DE", List.of("DE", "FR")))
        .thenReturn(buildDiagnosisKeys(6,
            LocalDateTime.of(2020, 7, 15, 12, 0, 0), 10, "FR", List.of("DE", "FR")))
        .thenReturn(buildDiagnosisKeys(6,
            LocalDateTime.of(2020, 7, 15, 12, 0, 0), 10, "NL", List.of("DE", "FR", "NL", "IE")))
        .thenReturn(buildDiagnosisKeys(6,
            LocalDateTime.of(2020, 7, 15, 12, 0, 0), 10, "IE", List.of("DE", "FR", "NL", "IE")));

    testDataGeneration.run(null);
    verify(diagnosisKeyService, never()).saveDiagnosisKeys(captor.capture());
  }

  @Test
  void shouldStoreOnlyKeysForLastHour() {
    var now = LocalDateTime.of(2020, 7, 15, 12, 0, 0).toInstant(ZoneOffset.UTC);
    TimeUtils.setNow(now);

    when(diagnosisKeyService.getDiagnosisKeys())
        .thenReturn(buildDiagnosisKeys(6,
            LocalDateTime.of(2020, 7, 15, 11, 0, 0), 10))
        .thenReturn(buildDiagnosisKeys(6,
            LocalDateTime.of(2020, 7, 15, 11, 0, 0), 10, "FR", List.of("DE", "FR")))
        .thenReturn(buildDiagnosisKeys(6,
            LocalDateTime.of(2020, 7, 15, 11, 0, 0), 10, "NL", List.of("DE", "FR", "NL", "IE")))
        .thenReturn(buildDiagnosisKeys(6,
            LocalDateTime.of(2020, 7, 15, 11, 0, 0), 10, "IE", List.of("DE", "FR", "NL", "IE")));

    testDataGeneration.run(null);
    verify(diagnosisKeyService, times(distributionServiceConfig.getSupportedCountries().length))
        .saveDiagnosisKeys(captor.capture());
    Assert.assertTrue(captor.getValue().stream()
        .allMatch(k -> k.getSubmissionTimestamp() != 443003));
  }

  @Test
  void shouldGenerateValuesForGivenCountry() {
    DistributionServiceConfig serviceConfigSpy = spy(distributionServiceConfig);
    serviceConfigSpy.setSupportedCountries(new String[]{"FR"});
    testDataGeneration = new TestDataGeneration(diagnosisKeyService, serviceConfigSpy);
    var now = LocalDateTime.of(2020, 7, 15, 12, 0, 0).toInstant(ZoneOffset.UTC);
    TimeUtils.setNow(now);

    when(diagnosisKeyService.getDiagnosisKeys()).thenReturn(Collections.emptyList());

    testDataGeneration.run(null);
    verify(diagnosisKeyService, atMostOnce()).saveDiagnosisKeys(captor.capture());
    Assert.assertTrue(captor.getValue().stream()
        .allMatch(k -> k.getOriginCountry().equals("FR")));
  }

  @Test
  void shouldNotGenerateAnyKeysForSupportedCountries() {
    testDataGeneration = new TestDataGeneration(diagnosisKeyService, distributionServiceConfig);
    var now = LocalDateTime.of(2020, 7, 15, 12, 0, 0).toInstant(ZoneOffset.UTC);
    TimeUtils.setNow(now);

    when(diagnosisKeyService.getDiagnosisKeys())
        .thenReturn(buildDiagnosisKeys(6, LocalDateTime.of(2020, 7, 15, 12, 0, 0), 10, "DE", List.of("DE", "FR")))
        .thenReturn(buildDiagnosisKeys(6, LocalDateTime.of(2020, 7, 15, 12, 0, 0), 10, "FR", List.of("DE", "FR")))
        .thenReturn(buildDiagnosisKeys(6,
            LocalDateTime.of(2020, 7, 15, 12, 0, 0), 10, "NL", List.of("DE", "FR", "NL", "IE")))
        .thenReturn(buildDiagnosisKeys(6,
            LocalDateTime.of(2020, 7, 15, 12, 0, 0), 10, "IE", List.of("DE", "FR", "NL", "IE")));

    testDataGeneration.run(null);
    verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
  }

  @Test
  void shouldGenerateAnyKeysForSupportedCountries() {
    testDataGeneration = new TestDataGeneration(diagnosisKeyService, distributionServiceConfig);
    var now = LocalDateTime.of(2020, 7, 15, 12, 0, 0).toInstant(ZoneOffset.UTC);
    TimeUtils.setNow(now);

    when(diagnosisKeyService.getDiagnosisKeys())
        .thenReturn(buildDiagnosisKeys(6, LocalDateTime.of(2020, 7, 14, 12, 0, 0), 10, "DE", List.of("DE", "FR")))
        .thenReturn(buildDiagnosisKeys(6, LocalDateTime.of(2020, 7, 14, 12, 0, 0), 10, "FR", List.of("DE", "FR")));

    testDataGeneration.run(null);
    verify(diagnosisKeyService, times(distributionServiceConfig.getSupportedCountries().length))
        .saveDiagnosisKeys(any());
  }
}
