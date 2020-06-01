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

package app.coronawarn.server.common.persistence.domain;

import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.assertDiagnosisKeysEqual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@DataJpaTest
class DiagnosisKeyServiceMockedRepositoryTest {

  static final byte[] expKeyData = "16-bytelongarray".getBytes(Charset.defaultCharset());
  static final int expRollingStartIntervalNumber = 73800;
  static final int expTransmissionRiskLevel = 1;

  @Autowired
  private DiagnosisKeyService diagnosisKeyService;

  @MockBean
  private DiagnosisKeyRepository diagnosisKeyRepository;

  @Test
  void testKeyRetrievalWithInvalidDbEntries() {
    DiagnosisKey invalidKey1 = invalidKey(1L);
    DiagnosisKey invalidKey2 = invalidKey(3L);
    var expKeys = List.of(invalidKey1, invalidKey2);

    mockInvalidKeyInDb(expKeys);

    List<DiagnosisKey> actualKeys = diagnosisKeyService.getDiagnosisKeys();
    assertThat(actualKeys.isEmpty()).isTrue();
  }

  @Test
  void testKeyRetrievalWithInvalidAndValidDbEntries() {
    DiagnosisKey invalidKey1 = invalidKey(1L);
    DiagnosisKey invalidKey2 = invalidKey(3L);
    var expKeys = new ArrayList<>(List.of(
        validKey(2L),
        invalidKey1,
        validKey(0L),
        invalidKey2));

    mockInvalidKeyInDb(expKeys);

    List<DiagnosisKey> actualKeys = diagnosisKeyService.getDiagnosisKeys();
    expKeys.remove(invalidKey1);
    expKeys.remove(invalidKey2);
    assertDiagnosisKeysEqual(expKeys, actualKeys);
  }

  private void mockInvalidKeyInDb(List<DiagnosisKey> keys) {
    when(diagnosisKeyRepository.findAll(Sort.by(Direction.ASC, "submissionTimestamp"))).thenReturn(keys);
  }

  private DiagnosisKey validKey(long expSubmissionTimestamp) {
    return new DiagnosisKey(expKeyData, expRollingStartIntervalNumber,
        DiagnosisKey.EXPECTED_ROLLING_PERIOD, expTransmissionRiskLevel, expSubmissionTimestamp);
  }

  private DiagnosisKey invalidKey(long expSubmissionTimestamp) {
    byte[] expKeyData = "17--bytelongarray".getBytes(Charset.defaultCharset());
    return new DiagnosisKey(expKeyData, expRollingStartIntervalNumber,
        DiagnosisKey.EXPECTED_ROLLING_PERIOD, expTransmissionRiskLevel, expSubmissionTimestamp);
  }
}
