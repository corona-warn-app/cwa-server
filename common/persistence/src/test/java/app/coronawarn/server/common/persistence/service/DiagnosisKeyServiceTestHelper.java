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

package app.coronawarn.server.common.persistence.service;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.VerificationType;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DiagnosisKeyServiceTestHelper {

  public static void assertDiagnosisKeysEqual(List<DiagnosisKey> expKeys,
      List<DiagnosisKey> actKeys) {
    assertThat(actKeys).withFailMessage("Cardinality mismatch").hasSameSizeAs(expKeys);

    for (int i = 0; i < expKeys.size(); i++) {
      var expKey = expKeys.get(i);
      var actKey = actKeys.get(i);

      assertThat(actKey.getKeyData()).withFailMessage("keyData mismatch")
          .isEqualTo(expKey.getKeyData());
      assertThat(actKey.getRollingStartIntervalNumber()).withFailMessage("rollingStartIntervalNumber mismatch")
          .isEqualTo(expKey.getRollingStartIntervalNumber());
      assertThat(actKey.getRollingPeriod()).withFailMessage("rollingPeriod mismatch")
          .isEqualTo(expKey.getRollingPeriod());
      assertThat(actKey.getTransmissionRiskLevel())
          .withFailMessage("transmissionRiskLevel mismatch")
          .isEqualTo(expKey.getTransmissionRiskLevel());
      assertThat(actKey.getSubmissionTimestamp()).withFailMessage("submissionTimestamp mismatch")
          .isEqualTo(expKey.getSubmissionTimestamp());
    }
  }

  public static DiagnosisKey buildDiagnosisKeyForSubmissionTimestamp(long submissionTimeStamp,
      String countryCode, List<String> visitedCountries, VerificationType verificationType) {
    byte[] randomBytes = new byte[16];
    Random random = new Random(submissionTimeStamp);
    random.nextBytes(randomBytes);
    return DiagnosisKey.builder()
        .withKeyData(randomBytes)
        .withRollingStartIntervalNumber(600)
        .withTransmissionRiskLevel(2)
        .withSubmissionTimestamp(submissionTimeStamp)
        .withCountryCode(countryCode)
        .withVisitedCountries(visitedCountries)
        .withVerificationType(verificationType)
        .build();
  }

  public static DiagnosisKey buildDiagnosisKeyForSubmissionTimestamp(long submissionTimeStamp) {
    return buildDiagnosisKeyForSubmissionTimestamp(submissionTimeStamp, "DE", Collections.singletonList("DE"), VerificationType.LAB_VERIFIED);
  }

  public static DiagnosisKey buildDiagnosisKeyForDateTime(OffsetDateTime dateTime) {
    return buildDiagnosisKeyForSubmissionTimestamp(dateTime.toEpochSecond() / 3600);
  }

  public static DiagnosisKey buildDiagnosisKeyForDateTime(OffsetDateTime dateTime,
      String countryCode, List<String> visitedCountries, VerificationType verificationType) {
    return buildDiagnosisKeyForSubmissionTimestamp(dateTime.toEpochSecond() / 3600, countryCode, visitedCountries, verificationType);
  }
}
