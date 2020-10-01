/*
 * ---license-start Corona-Warn-App --- Copyright (C) 2020 SAP SE and all other contributors ---
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License. ---license-end
 */

package app.coronawarn.server.services.download.validation;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.services.download.FederationBatchTestHelper;

class ValidFederationKeyFilterTest {

  @ParameterizedTest
  @ValueSource(ints = {-15, -17, 4001})
  void checkFilterRejectsWhenDaysSinceOnsetOfSymptomsNotInRange(int invalidDsos) {
    ValidFederationKeyFilter validator = new ValidFederationKeyFilter();
    DiagnosisKey mockedFederationKey =
        FederationBatchTestHelper.createFederationDiagnosisKey("test-keydata", invalidDsos);

    assertThat(validator.isValid(mockedFederationKey)).isFalse();
  }

  @Test
  void checkFilterRejectsWhenDaysSinceOnsetOfSymptomsIsMissing() {
    ValidFederationKeyFilter validator = new ValidFederationKeyFilter();
    DiagnosisKey mockedFederationKey = FederationBatchTestHelper
        .createFederationDiagnosisKeyWithoutDaysSinceSymptoms("test-keydata");

    assertThat(validator.isValid(mockedFederationKey)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 8, -14, 3986})
  void checkFilterAcceptsWhenDaysSinceOnsetOfSymptomsInRange(int validDsos) {
    ValidFederationKeyFilter validator = new ValidFederationKeyFilter();
    DiagnosisKey mockedFederationKey =
        FederationBatchTestHelper.createFederationDiagnosisKey("test-keydata", validDsos);

    assertThat(validator.isValid(mockedFederationKey)).isTrue();
  }
}
