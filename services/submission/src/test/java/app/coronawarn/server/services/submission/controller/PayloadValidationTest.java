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

import static app.coronawarn.server.services.submission.controller.RequestExecutor.*;
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
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"disable-ssl-client-verification", "disable-ssl-client-verification-verify-hostname"})
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
    ResponseEntity<Void> actResponse = executor.executePost(Lists.emptyList());
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void check400ResponseStatusForTooManyKeys() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithTooManyKeys());

    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  private Collection<TemporaryExposureKey> buildPayloadWithTooManyKeys() {
    ArrayList<TemporaryExposureKey> tooMany = new ArrayList<>();
    for (int i = 0; i <= 20; i++) {
      tooMany.add(buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(2) + i * DiagnosisKey.MAX_ROLLING_PERIOD , 3));
    }
    return tooMany;
  }

  @Test
  void check400ResponseStatusForDuplicateStartIntervalNumber() {
    int rollingStartIntervalNumber = createRollingStartIntervalNumber(2);
    var keysWithDuplicateStartIntervalNumber = Lists.list(
        buildTemporaryExposureKey(VALID_KEY_DATA_1, rollingStartIntervalNumber, 1),
        buildTemporaryExposureKey(VALID_KEY_DATA_2, rollingStartIntervalNumber, 2));

    ResponseEntity<Void> actResponse = executor.executePost(keysWithDuplicateStartIntervalNumber);

    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void check200ResponseStatusForGapsInTimeIntervals() {
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(6);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + DiagnosisKey.MAX_ROLLING_PERIOD;
    int rollingStartIntervalNumber3 = rollingStartIntervalNumber2 + 2 * DiagnosisKey.MAX_ROLLING_PERIOD;
    var keysWithGapsInStartIntervalNumber = Lists.list(
        buildTemporaryExposureKey(VALID_KEY_DATA_1, rollingStartIntervalNumber1, 1),
        buildTemporaryExposureKey(VALID_KEY_DATA_3, rollingStartIntervalNumber3, 3),
        buildTemporaryExposureKey(VALID_KEY_DATA_2, rollingStartIntervalNumber2, 2));

    ResponseEntity<Void> actResponse = executor.executePost(keysWithGapsInStartIntervalNumber);

    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @Test
  void check400ResponseStatusForOverlappingTimeIntervals() {
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(6);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + (DiagnosisKey.MAX_ROLLING_PERIOD / 2);
    var keysWithOverlappingStartIntervalNumber = Lists.list(
        buildTemporaryExposureKey(VALID_KEY_DATA_1, rollingStartIntervalNumber1, 1),
        buildTemporaryExposureKey(VALID_KEY_DATA_2, rollingStartIntervalNumber2, 2));

    ResponseEntity<Void> actResponse = executor.executePost(keysWithOverlappingStartIntervalNumber);

    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void check200ResponseStatusForValidSubmissionPayload() {
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(6);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + DiagnosisKey.MAX_ROLLING_PERIOD;
    int rollingStartIntervalNumber3 = rollingStartIntervalNumber2 + DiagnosisKey.MAX_ROLLING_PERIOD;
    var keysWithValidStartIntervalNumber = Lists.list(
        buildTemporaryExposureKey(VALID_KEY_DATA_1, rollingStartIntervalNumber1, 1),
        buildTemporaryExposureKey(VALID_KEY_DATA_3, rollingStartIntervalNumber3, 3),
        buildTemporaryExposureKey(VALID_KEY_DATA_2, rollingStartIntervalNumber2, 2));

    ResponseEntity<Void> actResponse = executor.executePost(keysWithValidStartIntervalNumber);

    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @Test
  void check200ResponseStatusForMoreThan14KeysWithFlexibleRollingPeriodValidSubmissionPayload() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithMoreThan14KeysAndFlexibleRollingPeriod());

    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  private Collection<TemporaryExposureKey> buildPayloadWithMoreThan14KeysAndFlexibleRollingPeriod() {
    ArrayList<TemporaryExposureKey> flexibleRollingPeriodKeys = new ArrayList<>();
    int counter = 0;
    for ( ; counter <= 20; counter++) {
      flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
          createRollingStartIntervalNumber(2) - counter * DiagnosisKey.MAX_ROLLING_PERIOD, 3, 144));
    }
    for ( ; counter < 30; counter++) {
      flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
          createRollingStartIntervalNumber(2) - counter * DiagnosisKey.MAX_ROLLING_PERIOD, 3, 133));

    }
    return flexibleRollingPeriodKeys;
  }

  @Test
    //Check duplicate start interval number
  void check200ResponseStatusForDuplicateStartDateWithFlexibleRollingPeriod() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithDuplicateStartDateWithFlexibleRollingPeriod());

    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }
  private Collection<TemporaryExposureKey> buildPayloadWithDuplicateStartDateWithFlexibleRollingPeriod() {
    ArrayList<TemporaryExposureKey> flexibleRollingPeriodKeys = new ArrayList<>();

    for (int i=0; i <= 10; i++) {
      flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
          createRollingStartIntervalNumber(2), 3, 100));
    }
    return flexibleRollingPeriodKeys;
  }

  @Test
  void check400ResponseStatusWithTwoKeysOneWithDefaultRollingPeriodAndOneWithFlexible() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithTwoKeysOneWithDefaultRollingPeriodAndOneWithFlexible());

    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  private Collection<TemporaryExposureKey> buildPayloadWithTwoKeysOneWithDefaultRollingPeriodAndOneWithFlexible() {
    ArrayList<TemporaryExposureKey> flexibleRollingPeriodKeys = new ArrayList<>();

    flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(2), 3, 100));
    flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(2), 3, 144));


    return flexibleRollingPeriodKeys;
  }

  @Test
  void check200ResponseStatusWithTwoKeysWithFlexibleRollingPeriod() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithTwoKeysWithFlexibleRollingPeriod());

    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  private Collection<TemporaryExposureKey> buildPayloadWithTwoKeysWithFlexibleRollingPeriod() {
    ArrayList<TemporaryExposureKey> flexibleRollingPeriodKeys = new ArrayList<>();

    flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(2), 3, 100));
    flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(2), 3, 44));


    return flexibleRollingPeriodKeys;
  }
}
