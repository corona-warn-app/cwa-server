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

package app.coronawarn.server.services.federation.upload;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static java.util.Collections.emptyList;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DiagnosisKeyBatchAssembler.class}, initializers = ConfigFileApplicationContextInitializer.class)
class DiagnosisKeyBatchAssemblerTest {

  @Autowired
  DiagnosisKeyBatchAssembler diagnosisKeyBatchAssembler;

  private void assertKeysAreEqual(DiagnosisKey persistenceKey, app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey exportKey) {
    Assertions.assertArrayEquals(persistenceKey.getKeyData(), exportKey.getKeyData().toByteArray(),
        "Key Data should be the same");
    Assertions.assertArrayEquals(persistenceKey.getVisitedCountries().toArray(), exportKey.getVisitedCountriesList().toArray(),
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

  private DiagnosisKey makeFakeKey(boolean consent) {
    return DiagnosisKey.builder()
        .withKeyData(new byte[16])
        .withRollingStartIntervalNumber(1)
        .withTransmissionRiskLevel(2)
        .withCountryCode("DE")
        .withConsentToFederation(consent)
        .withReportType(ReportType.CONFIRMED_CLINICAL_DIAGNOSIS)
        .withRollingPeriod(144)
        .withSubmissionTimestamp(LocalDateTime.of(2020, 7, 15, 12, 0, 0).toEpochSecond(ZoneOffset.UTC) / 3600)
        .withVisitedCountries(List.of("DE"))
        .build();
  }

  private DiagnosisKey makeFakeKey() {
    return this.makeFakeKey(true);
  }

  @Test
  public void shouldReturnEmptyListIfNoKeysGiven() {
    var result = diagnosisKeyBatchAssembler.assembleDiagnosisKeyBatch(emptyList());
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnSingleKeyInPackage() {
    var fakeKey = makeFakeKey();
    var result = diagnosisKeyBatchAssembler.assembleDiagnosisKeyBatch(List.of(fakeKey));
    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals(1, result.get(0).getKeysCount());
    this.assertKeysAreEqual(fakeKey, result.get(0).getKeys(0));
  }

  @Test
  void shouldReturnMultipleKeysInPackage() {
    var result = diagnosisKeyBatchAssembler.assembleDiagnosisKeyBatch(List.of(makeFakeKey(), makeFakeKey()));
    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals(2, result.get(0).getKeysCount());
  }

  @Test
  void shouldNotPackageKeysIfConsentFlagIsNotSet() {
    var result = diagnosisKeyBatchAssembler.assembleDiagnosisKeyBatch(List.of(makeFakeKey(), makeFakeKey(false)));
    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals(1, result.get(0).getKeysCount());
  }


}
