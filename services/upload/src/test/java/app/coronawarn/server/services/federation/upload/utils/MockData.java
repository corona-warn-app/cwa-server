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

package app.coronawarn.server.services.federation.upload.utils;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import java.security.SecureRandom;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MockData {

  public static final String TEST_ORIGIN_COUNTRY = "DE";
  public static final SecureRandom random = new SecureRandom();

  public static List<FederationUploadKey> generateRandomUploadKeys(boolean consentToShare, int numberOfKeys) {
    return IntStream.range(0, numberOfKeys)
        .mapToObj(ignore -> generateRandomUploadKey(consentToShare))
        .collect(Collectors.toList());
  }

  public static FederationUploadKey generateRandomUploadKey(boolean consentToShare) {
    return FederationUploadKey.from(generateRandomDiagnosisKey(consentToShare));
  }

  public static List<DiagnosisKey> generateRandomDiagnosisKeys(boolean consentToShare, int numberOfKeys) {
    return IntStream.range(0, numberOfKeys)
        .mapToObj(ignore -> generateRandomDiagnosisKey(consentToShare))
        .collect(Collectors.toList());
  }

  public static DiagnosisKey generateRandomDiagnosisKey(boolean consentToShare) {
    return DiagnosisKey.builder()
        .withKeyData(randomByteData())
        .withRollingStartIntervalNumber(1)
        .withTransmissionRiskLevel(2)
        .withConsentToFederation(consentToShare)
        .withCountryCode(TEST_ORIGIN_COUNTRY)
        .withDaysSinceOnsetOfSymptoms(randomDaysSinceOnsetOfSymptoms())
        .withSubmissionTimestamp(12)
        .withVisitedCountries(Set.of("FR", "DK"))
        .withReportType(ReportType.CONFIRMED_TEST)
        .build();
  }

  private static Integer randomDaysSinceOnsetOfSymptoms() {
    return random.nextInt(13);
  }

  private static byte[] randomByteData() {
    byte[] keyData = new byte[16];
    random.nextBytes(keyData);
    return keyData;
  }
}
