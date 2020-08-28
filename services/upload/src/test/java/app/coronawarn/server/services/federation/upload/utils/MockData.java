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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;

public class MockData {

  public static final String TEST_ORIGIN_COUNTRY = "DE";

  public static List<DiagnosisKey> generateRandomDiagnosisKeys(boolean consentToShare, int numberOfKeys) {
    List<DiagnosisKey> fakeKeys = new ArrayList<DiagnosisKey>();
    while (numberOfKeys >= 0) {
      fakeKeys.add(generateRandomDiagnosisKey(consentToShare));
      numberOfKeys--;
    }
    return fakeKeys;
  }

  public static DiagnosisKey generateRandomDiagnosisKey(boolean consentToShare) {
   return DiagnosisKey.builder().withKeyData(randomByteData())
                            .withRollingStartIntervalNumber(1)
                            .withTransmissionRiskLevel(2)
                            .withConsentToFederation(consentToShare)
                            .withCountryCode(TEST_ORIGIN_COUNTRY)
                            .withDaysSinceOnsetOfSymptoms(1)
                            .withSubmissionTimestamp(12)
                            .withVisitedCountries(List.of("FR","DK"))
                            .withReportType(ReportType.CONFIRMED_TEST)
                            .build();
  }

  private static byte[] randomByteData() {
    byte[] keydata = new byte[16];
    new Random().nextBytes(keydata);
    return keydata;
  }
}
