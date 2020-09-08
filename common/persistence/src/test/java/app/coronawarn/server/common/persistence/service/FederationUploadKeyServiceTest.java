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

import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.assertDiagnosisKeysEqual;
import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.buildDiagnosisKeyForSubmissionTimestamp;
import static org.mockito.Mockito.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import app.coronawarn.server.common.persistence.service.common.ExpirationPolicy;

@DataJdbcTest
class FederationUploadKeyServiceTest {

  @Autowired
  private FederationUploadKeyService uploadKeyService;

  @MockBean
  private FederationUploadKeyRepository uploadKeyRepository;

  @MockBean
  private KeySharingPoliciesChecker keySharingPoliciesChecker;


  @Test
  void shouldRetrieveKeysWithConsentOnly() {
    var testKeys = new ArrayList<>(List.of(
        buildDiagnosisKeyForSubmissionTimestamp(1000L, true),
        buildDiagnosisKeyForSubmissionTimestamp(2000L, false)));

    when(uploadKeyRepository.findAllUploadableKeys()).thenReturn(testKeys);
    when(keySharingPoliciesChecker.canShareKeyAtTime(any(), any(), any())).thenReturn(true);

    var actKeys = uploadKeyService.getPendingUploadKeys(ExpirationPolicy.of(0, ChronoUnit.MINUTES));
    Assertions.assertThat(actKeys).hasSize(1);
    assertDiagnosisKeysEqual(testKeys.get(0), actKeys.get(0));
  }

  @Test
  void shouldRetrieveExpiredKeysOnly() {
    DiagnosisKey key1 = buildDiagnosisKeyForSubmissionTimestamp(1000L, true);
    DiagnosisKey key2 = buildDiagnosisKeyForSubmissionTimestamp(2000L, false);
    var testKeys = new ArrayList<>(List.of(key1, key2));

    when(uploadKeyRepository.findAllUploadableKeys()).thenReturn(testKeys);
    when(keySharingPoliciesChecker.canShareKeyAtTime(eq(key1), any(), any())).thenReturn(true);
    when(keySharingPoliciesChecker.canShareKeyAtTime(eq(key2), any(), any())).thenReturn(false);

    var actKeys = uploadKeyService.getPendingUploadKeys(ExpirationPolicy.of(120, ChronoUnit.MINUTES));
    Assertions.assertThat(actKeys).hasSize(1);
    assertDiagnosisKeysEqual(testKeys.get(0), actKeys.get(0));
  }
}
