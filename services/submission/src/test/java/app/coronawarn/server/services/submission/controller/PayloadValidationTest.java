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

import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
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
    when(this.tanVerifier.verifyTan(anyString())).thenReturn(true);
  }

  @Autowired
  RequestExecutor executor;

  @Test
  void check400ResponseStatusForMissingKeys() {
    ResponseEntity<Void> actResponse = executor.executePost(Lists.emptyList());
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void check400ResponseStatusForTooManyKeysWithFixedRollingPeriod() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithTooManyKeys());
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  private Collection<TemporaryExposureKey> buildPayloadWithTooManyKeys() {
    ArrayList<TemporaryExposureKey> tooMany = new ArrayList<>();
    for (int i = 0; i <= 20; i++) {
      tooMany.add(buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(2) + i * DiagnosisKey.MAX_ROLLING_PERIOD , 3,
          ReportType.CONFIRMED_CLINICAL_DIAGNOSIS,1));
    }
    return tooMany;
  }

  @ParameterizedTest
  @ValueSource(ints = {-15, -100, 16, 20})
  void check400ResponseStatusForDsosNotInRange(int invalidDsosValue) {
    ResponseEntity<Void> actResponse = executor.executePost(buildKeysWithDaysSinceSymptoms(invalidDsosValue));
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @ParameterizedTest
  @ValueSource(ints = {-14, -9, 0, 14})
  void check200ResponseStatusForDsosInRange(int validDsosValue) {
    ResponseEntity<Void> actResponse = executor.executePost(buildKeysWithDaysSinceSymptoms(validDsosValue));
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  private Collection<TemporaryExposureKey>  buildKeysWithDaysSinceSymptoms(int dsos) {
    return List.of(buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(2), 3,
        ReportType.CONFIRMED_CLINICAL_DIAGNOSIS, dsos),
        // also add a key without DSOS since this can happen in production and should be supported
        buildTemporaryExposureKey(VALID_KEY_DATA_1,
            createRollingStartIntervalNumber(2) + DiagnosisKey.MAX_ROLLING_PERIOD, 3,
            ReportType.CONFIRMED_CLINICAL_DIAGNOSIS, null));
  }

  private Collection<TemporaryExposureKey>  buildKeysWithoutDaysSinceSymptomsAndTransmissionRiskLevel() {
    return List.of(
        buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(2), null,
            ReportType.CONFIRMED_CLINICAL_DIAGNOSIS, null),
        // also add a key without DSOS since this can happen in production and should be supported
        buildTemporaryExposureKey(VALID_KEY_DATA_1,
            createRollingStartIntervalNumber(2) + +DiagnosisKey.MAX_ROLLING_PERIOD, null,
            ReportType.CONFIRMED_CLINICAL_DIAGNOSIS, null));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 9, 12})
  void check400ResponseStatusForTrlNotAccepted(int invalidTrlValue) {
    ResponseEntity<Void> actResponse = executor.executePost(buildKeysWithTransmissionRiskLevel(invalidTrlValue));
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 3, 5, 6, 8})
  void check200ResponseStatusForTrlAccepted(int validTrlValue) {
    ResponseEntity<Void> actResponse = executor.executePost(buildKeysWithTransmissionRiskLevel(validTrlValue));
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  private Collection<TemporaryExposureKey> buildKeysWithTransmissionRiskLevel(int trl) {
    return List.of(buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(2), trl,
        ReportType.CONFIRMED_CLINICAL_DIAGNOSIS, 1),
        // also add a key without TRL since this can happen in production and should be supported
        buildTemporaryExposureKey(VALID_KEY_DATA_1,
            createRollingStartIntervalNumber(2) + +DiagnosisKey.MAX_ROLLING_PERIOD, null,
            ReportType.CONFIRMED_CLINICAL_DIAGNOSIS, 1));
  }

  @Test
  void check400ResponseStatusForMissingTrlAndDsos() {
    ResponseEntity<Void> actResponse = executor.executePost(buildKeysWithoutDaysSinceSymptomsAndTransmissionRiskLevel());
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void check400ResponseStatusForKeysWithFixedRollingPeriodAndDuplicateStartIntervals() {
    int rollingStartIntervalNumber = createRollingStartIntervalNumber(2);
    var keysWithDuplicateStartIntervalNumber = Lists.list(
        buildTemporaryExposureKey(VALID_KEY_DATA_1, rollingStartIntervalNumber, 1,
            ReportType.CONFIRMED_CLINICAL_DIAGNOSIS,1),
        buildTemporaryExposureKey(VALID_KEY_DATA_2, rollingStartIntervalNumber, 2, ReportType.CONFIRMED_CLINICAL_DIAGNOSIS,1));

    ResponseEntity<Void> actResponse = executor.executePost(keysWithDuplicateStartIntervalNumber);

    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void check200ResponseStatusForGapsInTimeIntervalsOfKeysWithFixedRollingPeriod() {
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(6);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + DiagnosisKey.MAX_ROLLING_PERIOD;
    int rollingStartIntervalNumber3 = rollingStartIntervalNumber2 + 3 * DiagnosisKey.MAX_ROLLING_PERIOD;
    var keysWithGapsInStartIntervalNumber = Lists.list(
        buildTemporaryExposureKey(VALID_KEY_DATA_1, rollingStartIntervalNumber1, 1, ReportType.CONFIRMED_CLINICAL_DIAGNOSIS,1),
        buildTemporaryExposureKey(VALID_KEY_DATA_3, rollingStartIntervalNumber3, 3, ReportType.CONFIRMED_CLINICAL_DIAGNOSIS,1),
        buildTemporaryExposureKey(VALID_KEY_DATA_2, rollingStartIntervalNumber2, 3, ReportType.CONFIRMED_CLINICAL_DIAGNOSIS,1));

    ResponseEntity<Void> actResponse = executor.executePost(keysWithGapsInStartIntervalNumber);

    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @Test
  void check200ResponseStatusForGapsInTimeIntervalsOfKeysWithFlexibleRollingPeriod() {
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(6);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + DiagnosisKey.MAX_ROLLING_PERIOD;
    int rollingStartIntervalNumber3 = rollingStartIntervalNumber2 + 3 * DiagnosisKey.MAX_ROLLING_PERIOD;
    var keysWithGapsInStartIntervalNumber = Lists.list(
        buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1, rollingStartIntervalNumber1, 1, 54),
        buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1, rollingStartIntervalNumber1, 1, 90),
        buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_3, rollingStartIntervalNumber3, 3, 133),
        buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_2, rollingStartIntervalNumber2, 3, 144));

    ResponseEntity<Void> actResponse = executor.executePost(keysWithGapsInStartIntervalNumber);

    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @ParameterizedTest
  @MethodSource("app.coronawarn.server.services.submission.controller.TEKDatasetGeneration#getOverlappingTestDatasets")
  void check400ResponseStatusForOverlappingTimeIntervalsI(List<TemporaryExposureKey> dataset) {
    ResponseEntity<Void> actResponse = executor.executePost(dataset);
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @ParameterizedTest
  @MethodSource("app.coronawarn.server.services.submission.controller.TEKDatasetGeneration#getRollingPeriodDatasets")
  void check200ResponseStatusForValidSubmissionPayload(List<TemporaryExposureKey> dataset) {
    ResponseEntity<Void> actResponse = executor.executePost(dataset);
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  /**
   *  This test generates a payload with keys for the past 30 days. It verifies that validation passes even
   *  though keys older than <code>application.yml/retention-period</code> would not be stored.
   */
  @Test
  void check200ResponseStatusForMoreThan14KeysWithValidFlexibleRollingPeriod() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithMoreThan14KeysAndFlexibleRollingPeriod());
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  private Collection<TemporaryExposureKey> buildPayloadWithMoreThan14KeysAndFlexibleRollingPeriod() {
    ArrayList<TemporaryExposureKey> flexibleRollingPeriodKeys = new ArrayList<>();
    /* Generate keys with fixed rolling period (144) for the past 20 days */
    for (int i = 0 ; i < 20; i++) {
      flexibleRollingPeriodKeys.add(buildTemporaryExposureKey(VALID_KEY_DATA_1,
          createRollingStartIntervalNumber(2) - i * DiagnosisKey.MAX_ROLLING_PERIOD, 3, ReportType.CONFIRMED_CLINICAL_DIAGNOSIS,1));
    }
    /* Generate another 10 keys with flexible rolling period (<144) */
    for (int i = 20 ; i < 30; i++) {
      flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
          createRollingStartIntervalNumber(2) - i * DiagnosisKey.MAX_ROLLING_PERIOD, 3, 133));

    }
    return flexibleRollingPeriodKeys;
  }

  @Test
  void check400ResponseStatusWhenTwoKeysCumulateMoreThanMaxRollingPeriodInSameDay() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithKeysThatCumulateMoreThanMaxRollingPeriodPerDay());
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  private Collection<TemporaryExposureKey> buildPayloadWithKeysThatCumulateMoreThanMaxRollingPeriodPerDay() {
    ArrayList<TemporaryExposureKey> temporaryExposureKeys = new ArrayList<>();
    temporaryExposureKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(2), 3, 100));
    temporaryExposureKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(2), 3, 144));

    return temporaryExposureKeys;
  }

  @Test
  void check200ResponseStatusWithTwoKeysOneFlexibleAndOneDefaultOnDifferentDays() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithTwoKeysOneFlexibleAndOneDefaultOnDifferentDays());

    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  private Collection<TemporaryExposureKey> buildPayloadWithTwoKeysOneFlexibleAndOneDefaultOnDifferentDays() {
    ArrayList<TemporaryExposureKey> flexibleRollingPeriodKeys = new ArrayList<>();

    flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(2), 3, 100));
    flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(3), 3, 144));


    return flexibleRollingPeriodKeys;
  }

  @Test
  void check200ResponseStatusWhenKeysCumulateToMaxRollingPeriodInSameDay() {
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
