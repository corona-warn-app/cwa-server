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

package app.coronawarn.server.services.submission.controller;

import static app.coronawarn.server.services.submission.controller.RequestExecutor.VALID_KEY_DATA_1;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.VALID_KEY_DATA_2;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.VALID_KEY_DATA_3;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.buildOkHeaders;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.buildTemporaryExposureKey;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.createRollingStartIntervalNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import java.util.ArrayList;
import java.util.Collection;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PayloadValidationTest {

  @MockBean
  private TanVerifier tanVerifier;

  @BeforeEach
  public void setUpMocks() {
    when(tanVerifier.verifyTan(anyString())).thenReturn(true);
  }

  @Autowired
  RequestExecutor executor;

  @Test
  void check400ResponseStatusForMissingKeys() {
    ResponseEntity<Void> actResponse = executor.executeRequest(Lists.emptyList(), buildOkHeaders());
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void check400ResponseStatusForTooManyKeys() {
    ResponseEntity<Void> actResponse = executor.executeRequest(buildPayloadWithTooManyKeys(), buildOkHeaders());

    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  private Collection<TemporaryExposureKey> buildPayloadWithTooManyKeys() {
    ArrayList<TemporaryExposureKey> tooMany = new ArrayList<>();
    for (int i = 0; i <= 20; i++) {
      tooMany.add(buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(2), 3));
    }
    return tooMany;
  }

  @Test
  void check400ResponseStatusForDuplicateStartIntervalNumber() {
    int rollingStartIntervalNumber = createRollingStartIntervalNumber(2);
    var keysWithDuplicateStartIntervalNumber = Lists.list(
        buildTemporaryExposureKey(VALID_KEY_DATA_1, rollingStartIntervalNumber, 1),
        buildTemporaryExposureKey(VALID_KEY_DATA_2, rollingStartIntervalNumber, 2));

    ResponseEntity<Void> actResponse = executor.executeRequest(keysWithDuplicateStartIntervalNumber, buildOkHeaders());

    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void check400ResponseStatusForGapsInTimeIntervals() {
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(6);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + DiagnosisKey.EXPECTED_ROLLING_PERIOD;
    int rollingStartIntervalNumber3 = rollingStartIntervalNumber2 + 2 * DiagnosisKey.EXPECTED_ROLLING_PERIOD;
    var keysWithDuplicateStartIntervalNumber = Lists.list(
        buildTemporaryExposureKey(VALID_KEY_DATA_1, rollingStartIntervalNumber1, 1),
        buildTemporaryExposureKey(VALID_KEY_DATA_3, rollingStartIntervalNumber3, 3),
        buildTemporaryExposureKey(VALID_KEY_DATA_2, rollingStartIntervalNumber2, 2));

    ResponseEntity<Void> actResponse = executor.executeRequest(keysWithDuplicateStartIntervalNumber, buildOkHeaders());

    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void check400ResponseStatusForOverlappingTimeIntervals() {
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(6);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + (DiagnosisKey.EXPECTED_ROLLING_PERIOD / 2);
    var keysWithDuplicateStartIntervalNumber = Lists.list(
        buildTemporaryExposureKey(VALID_KEY_DATA_1, rollingStartIntervalNumber1, 1),
        buildTemporaryExposureKey(VALID_KEY_DATA_2, rollingStartIntervalNumber2, 2));

    ResponseEntity<Void> actResponse = executor.executeRequest(keysWithDuplicateStartIntervalNumber, buildOkHeaders());

    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void check200ResponseStatusForValidSubmissionPayload() {
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(6);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + DiagnosisKey.EXPECTED_ROLLING_PERIOD;
    int rollingStartIntervalNumber3 = rollingStartIntervalNumber2 + DiagnosisKey.EXPECTED_ROLLING_PERIOD;
    var keysWithDuplicateStartIntervalNumber = Lists.list(
        buildTemporaryExposureKey(VALID_KEY_DATA_1, rollingStartIntervalNumber1, 1),
        buildTemporaryExposureKey(VALID_KEY_DATA_3, rollingStartIntervalNumber3, 3),
        buildTemporaryExposureKey(VALID_KEY_DATA_2, rollingStartIntervalNumber2, 2));

    ResponseEntity<Void> actResponse = executor.executeRequest(keysWithDuplicateStartIntervalNumber, buildOkHeaders());

    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }
}
