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
import static java.time.ZoneOffset.UTC;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.common.persistence.service.common.ExpirationPolicy;

@DataJdbcTest
class FederationUploadKeyServiceTest {

  @Autowired
  private FederationUploadKeyService uploadKeyService;

  @MockBean
  private FederationUploadKeyRepository uploadKeyRepository;


  @Test
  void shouldRetrieveKeysWithConsentOnly() {
    var testKeys = new ArrayList<>(List.of(
        buildDiagnosisKeyForSubmissionTimestamp(1000L, true),
        buildDiagnosisKeyForSubmissionTimestamp(2000L, false)));
    Mockito.when(uploadKeyRepository.findAllUploadableKeys()).thenReturn(testKeys);
    var actKeys = uploadKeyService.getPendingUploadKeys(ExpirationPolicy.of(0, ChronoUnit.MINUTES));
    Assertions.assertEquals(1,actKeys.size());
    assertDiagnosisKeysEqual(testKeys.get(0), actKeys.get(0));
  }

  @Test
  void shouldRetrieveExpiredKeysOnly() {
    LocalDateTime fiveMinutesAgo = LocalDateTime.ofInstant(Instant.now(), UTC).minusMinutes(5);
    long currentHoursSinceEpoch = fiveMinutesAgo.toEpochSecond(UTC) / TimeUnit.HOURS.toSeconds(1);
    int rollingStart = Math.toIntExact(fiveMinutesAgo.toEpochSecond(UTC) / 600L);

    var testKeys = new ArrayList<>(List.of(
        buildDiagnosisKeyForSubmissionTimestamp(1000L, 600, true),
        buildDiagnosisKeyForSubmissionTimestamp(currentHoursSinceEpoch, rollingStart, true)));
    Mockito.when(uploadKeyRepository.findAllUploadableKeys()).thenReturn(testKeys);
    var actKeys = uploadKeyService.getPendingUploadKeys(ExpirationPolicy.of(10, ChronoUnit.MINUTES));
    Assertions.assertEquals(1,actKeys.size());
    assertDiagnosisKeysEqual(testKeys.get(0), actKeys.get(0));
  }
}
