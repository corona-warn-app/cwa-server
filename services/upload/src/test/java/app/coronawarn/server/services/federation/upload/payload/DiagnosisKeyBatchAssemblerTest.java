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

package app.coronawarn.server.services.federation.upload.payload;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

@EnableConfigurationProperties(value = UploadServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DiagnosisKeyBatchAssembler.class}, initializers = ConfigFileApplicationContextInitializer.class)
class DiagnosisKeyBatchAssemblerTest {

  private static volatile int minKeyThreshold;
  private static volatile int maxKeyCount;

  @Autowired
  DiagnosisKeyBatchAssembler diagnosisKeyBatchAssembler;

  @Autowired
  UploadServiceConfig uploadServiceConfig;

  @BeforeEach
  public void setup() {
    minKeyThreshold = uploadServiceConfig.getMinBatchKeyCount();
    maxKeyCount = uploadServiceConfig.getMaxBatchKeyCount();
  }

  private void assertKeysAreEqual(DiagnosisKey persistenceKey,
      app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey exportKey) {
    Assertions.assertArrayEquals(persistenceKey.getKeyData(), exportKey.getKeyData().toByteArray(),
        "Key Data should be the same");
    Assertions.assertArrayEquals(persistenceKey.getVisitedCountries().toArray(),
        exportKey.getVisitedCountriesList().toArray(),
        "Visited countries should be the same");
    Assertions.assertEquals(persistenceKey.getRollingPeriod(), exportKey.getRollingPeriod(),
        "Rolling Period should be the same");
    Assertions.assertEquals(persistenceKey.getReportType(), exportKey.getReportType(),
        "Verification Type should be the same");
    Assertions.assertEquals(persistenceKey.getTransmissionRiskLevel(), exportKey.getTransmissionRiskLevel(),
        "Transmission Risk Level should be the same");
    Assertions.assertEquals(persistenceKey.getOriginCountry(), exportKey.getOrigin(),
        "Origin Country should be the same");
  }

  private static FederationUploadKey makeFakeKey(boolean consent) {
    return FederationUploadKey.from(
        DiagnosisKey.builder()
        .withKeyData(new byte[16])
        .withRollingStartIntervalNumber(1)
        .withTransmissionRiskLevel(2)
        .withCountryCode("DE")
        .withConsentToFederation(consent)
        .withReportType(ReportType.CONFIRMED_CLINICAL_DIAGNOSIS)
        .withRollingPeriod(144)
        .withSubmissionTimestamp(LocalDateTime.of(2020, 7, 15, 12, 0, 0).toEpochSecond(ZoneOffset.UTC) / 3600)
        .withVisitedCountries(List.of("DE"))
        .build());
  }

  private static List<FederationUploadKey> makeFakeKeys(boolean consent, int numberOfKeys) {
    List<FederationUploadKey> keys = new ArrayList<>(numberOfKeys);
    while (numberOfKeys > 0) {
      keys.add(makeFakeKey(consent));
      numberOfKeys--;
    }
    return keys;
  }

  @Test
  void shouldReturnEmptyListIfNoKeysGiven() {
    var result = diagnosisKeyBatchAssembler.assembleDiagnosisKeyBatch(emptyList());
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnEmptyListIfLessThenThresholdKeysGiven() {
    var result = diagnosisKeyBatchAssembler.assembleDiagnosisKeyBatch(makeFakeKeys(true, minKeyThreshold - 1));
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void packagedKeysShouldContainInitialInformation() {
    var fakeKeys = makeFakeKeys(true, minKeyThreshold);
    var result = diagnosisKeyBatchAssembler.assembleDiagnosisKeyBatch(fakeKeys);

    Assertions.assertEquals(fakeKeys.size(),result.get(0).getKeysCount());
    // as keys are created equal we need to compare just the first two elements of each list
    assertKeysAreEqual(fakeKeys.get(0), result.get(0).getKeys(0));
  }

  @Test
  void shouldNotPackageKeysIfConsentFlagIsNotSet() {
    var dataset = makeFakeKeys(true, minKeyThreshold);
    dataset.add(makeFakeKey(false));
    var result = diagnosisKeyBatchAssembler.assembleDiagnosisKeyBatch(dataset);
    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals(minKeyThreshold, result.get(0).getKeysCount());
  }

  @ParameterizedTest
  @MethodSource("keysToPartitionAndBatchNumberExpectations")
  void shouldGenerateCorrectNumberOfBatches(List<FederationUploadKey> dataset, Integer expectedBatches) {
    var result = diagnosisKeyBatchAssembler.assembleDiagnosisKeyBatch(dataset);
    Assertions.assertEquals(expectedBatches, result.size());
  }

  /**
   * @return A stream of tuples which represents the dataset together with the
   * expectation required to test batch key partioning.
   */
  private static Stream<Arguments> keysToPartitionAndBatchNumberExpectations() {
    return Stream.of(
        Arguments.of(makeFakeKeys(true, minKeyThreshold - 1), 0),
        Arguments.of(makeFakeKeys(true, minKeyThreshold), 1),
        Arguments.of(makeFakeKeys(true, maxKeyCount), 1),
        Arguments.of(makeFakeKeys(true, maxKeyCount / 2), 1),
        Arguments.of(makeFakeKeys(true, maxKeyCount - 1), 1),
        Arguments.of(makeFakeKeys(true, maxKeyCount + 1), 2),
        Arguments.of(makeFakeKeys(true, 2 * maxKeyCount), 2),
        Arguments.of(makeFakeKeys(true, 3 * maxKeyCount), 3),
        Arguments.of(makeFakeKeys(true, 4 * maxKeyCount), 4),
        Arguments.of(makeFakeKeys(true, 2 * maxKeyCount + 1), 3),
        Arguments.of(makeFakeKeys(true, 2 * maxKeyCount + maxKeyCount / 2), 3),
        Arguments.of(makeFakeKeys(true, 2 * maxKeyCount - maxKeyCount / 2), 2)
    );
  }
}
