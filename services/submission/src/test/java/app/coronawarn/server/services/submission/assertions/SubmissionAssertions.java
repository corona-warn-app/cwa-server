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

package app.coronawarn.server.services.submission.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;

public final class SubmissionAssertions {

  public static void assertElementsCorrespondToEachOther(Collection<TemporaryExposureKey> submittedTemporaryExposureKeys,
      Collection<DiagnosisKey> savedDiagnosisKeys, SubmissionServiceConfig config) {

    Set<DiagnosisKey> submittedDiagnosisKeys = submittedTemporaryExposureKeys.stream()
        .map(submittedDiagnosisKey -> DiagnosisKey.builder().fromProtoBuf(submittedDiagnosisKey).build())
        .collect(Collectors.toSet());

    assertThat(savedDiagnosisKeys).hasSize(submittedDiagnosisKeys.size() * config.getRandomKeyPaddingMultiplier());
    assertThat(savedDiagnosisKeys).containsAll(submittedDiagnosisKeys);

    submittedDiagnosisKeys.forEach(submittedDiagnosisKey -> {
      List<DiagnosisKey> savedKeysForSingleSubmittedKey = savedDiagnosisKeys.stream()
          .filter(savedDiagnosisKey -> savedDiagnosisKey.getRollingPeriod() == submittedDiagnosisKey.getRollingPeriod())
          .filter(savedDiagnosisKey -> savedDiagnosisKey.getTransmissionRiskLevel() == submittedDiagnosisKey
              .getTransmissionRiskLevel())
          .filter(savedDiagnosisKey -> savedDiagnosisKey.getRollingStartIntervalNumber() == submittedDiagnosisKey
              .getRollingStartIntervalNumber())
          .collect(Collectors.toList());

      assertThat(savedKeysForSingleSubmittedKey).hasSize(config.getRandomKeyPaddingMultiplier());
      assertThat(savedKeysForSingleSubmittedKey.stream()
          .filter(savedKey -> Arrays.equals(savedKey.getKeyData(), submittedDiagnosisKey.getKeyData()))).hasSize(1);
      assertThat(savedKeysForSingleSubmittedKey)
          .allMatch(savedKey -> savedKey.getRollingPeriod() == submittedDiagnosisKey.getRollingPeriod());
      assertThat(savedKeysForSingleSubmittedKey).allMatch(savedKey -> savedKey
          .getRollingStartIntervalNumber() == submittedDiagnosisKey.getRollingStartIntervalNumber());
      assertThat(savedKeysForSingleSubmittedKey).allMatch(
          savedKey -> savedKey.getTransmissionRiskLevel() == submittedDiagnosisKey.getTransmissionRiskLevel());
    });
  }
}
