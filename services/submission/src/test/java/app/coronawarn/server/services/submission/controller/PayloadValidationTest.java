

package app.coronawarn.server.services.submission.controller;

import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.VALID_KEY_DATA_1;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildTemporaryExposureKey;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildTemporaryExposureKeyWithFlexibleRollingPeriod;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.createRollingStartIntervalNumber;
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
import java.util.stream.IntStream;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
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
    generatePayloadKeys(tooMany,101,4);
    return tooMany;
  }

  @ParameterizedTest
  @ValueSource(ints = {-15, -100, 4001})
  void check400ResponseStatusForDaysSinceSymptomsFieldNotInRange(int invalidDsosValue) {
    ResponseEntity<Void> actResponse = executor.executePost(buildKeysWithDaysSinceSymptoms(invalidDsosValue));
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @ParameterizedTest
  @ValueSource(ints = {-14, -9, 0, 3986})
  void check200ResponseStatusForDaysSinceSymptomsFieldInRange(int validDsosValue) {
    ResponseEntity<Void> actResponse = executor.executePost(buildKeysWithDaysSinceSymptoms(validDsosValue));
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  private Collection<TemporaryExposureKey> buildKeysWithDaysSinceSymptoms(int dsos) {
    return List.of(buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(2), 3,
        ReportType.CONFIRMED_TEST, dsos),
        // also add a key without DSOS since this can happen in production and should be supported
        buildTemporaryExposureKey(VALID_KEY_DATA_1,
            createRollingStartIntervalNumber(2) + DiagnosisKey.MAX_ROLLING_PERIOD, 3,
            ReportType.CONFIRMED_TEST, null));
  }

  private Collection<TemporaryExposureKey> buildKeysWithoutDaysSinceSymptomsAndTransmissionRiskLevel() {
    return List.of(
        buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(2), null,
            ReportType.CONFIRMED_TEST, null),
        // also add a key without DSOS since this can happen in production and should be supported
        buildTemporaryExposureKey(VALID_KEY_DATA_1,
            createRollingStartIntervalNumber(2) + +DiagnosisKey.MAX_ROLLING_PERIOD, null,
            ReportType.CONFIRMED_TEST, null));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 9, 12})
  void check400ResponseStatusForTransmissionRiskLevelNotAccepted(int invalidTrlValue) {
    ResponseEntity<Void> actResponse = executor.executePost(buildKeysWithTransmissionRiskLevel(invalidTrlValue));
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 3, 5, 6, 8})
  void check200ResponseStatusForTransmissionRiskLevelAccepted(int validTrlValue) {
    ResponseEntity<Void> actResponse = executor.executePost(buildKeysWithTransmissionRiskLevel(validTrlValue));
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  private Collection<TemporaryExposureKey> buildKeysWithTransmissionRiskLevel(int trl) {
    return List.of(buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(2), trl,
        ReportType.CONFIRMED_TEST, 1),
        // also add a key without TRL since this can happen in production and should be supported
        buildTemporaryExposureKey(VALID_KEY_DATA_1,
            createRollingStartIntervalNumber(2) + +DiagnosisKey.MAX_ROLLING_PERIOD, null,
            ReportType.CONFIRMED_TEST, 1));
  }

  @Test
  void check400ResponseStatusForMissingTransmissionRiskLevelAndDaysSinceSymptoms() {
    ResponseEntity<Void> actResponse = executor
        .executePost(buildKeysWithoutDaysSinceSymptomsAndTransmissionRiskLevel());
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @ParameterizedTest
  @MethodSource("app.coronawarn.server.services.submission.controller.TEKDatasetGeneration#getOverlappingTestDatasets")
  void check400ResponseStatusForOverlappingTimeIntervals(List<TemporaryExposureKey> dataset) {
    ResponseEntity<Void> actResponse = executor.executePost(dataset);
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @ParameterizedTest
  @MethodSource("app.coronawarn.server.services.submission.controller.TEKDatasetGeneration#getRollingPeriodDatasets")
  void check200ResponseStatusForValidSubmissionPayload(List<TemporaryExposureKey> dataset) {
    ResponseEntity<Void> actResponse = executor.executePost(dataset);
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @Test
  void check200ResponseStatusWithTwoKeysOnDifferentDays() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithTwoKeysOnDifferentDays());

    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  private Collection<TemporaryExposureKey> buildPayloadWithTwoKeysOnDifferentDays() {
    ArrayList<TemporaryExposureKey> flexibleRollingPeriodKeys = new ArrayList<>();

    flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(2), 3, 100));
    flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(3), 3, 144));

    return flexibleRollingPeriodKeys;
  }

  @Test
  void check200ResponseStatusWithOneValidKey() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithOneValidKey());

    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  private Collection<TemporaryExposureKey> buildPayloadWithOneValidKey() {
    ArrayList<TemporaryExposureKey> flexibleRollingPeriodKeys = new ArrayList<>();

    flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(1), 3, 100));

    return flexibleRollingPeriodKeys;
  }


  @Test
  void check200ResponseStatusWithTwoKeyFromToday() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithTwoKeysFromToday());

    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  private Collection<TemporaryExposureKey> buildPayloadWithTwoKeysFromToday() {
    ArrayList<TemporaryExposureKey> flexibleRollingPeriodKeys = new ArrayList<>();

    flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(0), 3, 100));
    flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(0), 3, 144));

    return flexibleRollingPeriodKeys;
  }

  @Test
  void check400ResponseStatusWithKeysInFuture() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithTwoKeysInFuture());

    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  private Collection<TemporaryExposureKey> buildPayloadWithTwoKeysInFuture() {
    ArrayList<TemporaryExposureKey> flexibleRollingPeriodKeys = new ArrayList<>();

    flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(-1), 3, 100));
    flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(-2), 3, 144));

    return flexibleRollingPeriodKeys;
  }

  @Test
  void check200ResponseStatusWithTwoKeysOneTodayOneYesterday() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithTwoKeysOneTodayOneYesterday());

    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  private Collection<TemporaryExposureKey> buildPayloadWithTwoKeysOneTodayOneYesterday() {
    ArrayList<TemporaryExposureKey> flexibleRollingPeriodKeys = new ArrayList<>();

    flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(0), 3, 100));
    flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(1), 3, 144));

    return flexibleRollingPeriodKeys;
  }

  @Test
  void check200ResponseStatusWithOutdatedKeys() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithOutdatedKeys());

    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  private Collection<TemporaryExposureKey> buildPayloadWithOutdatedKeys() {
    ArrayList<TemporaryExposureKey> flexibleRollingPeriodKeys = new ArrayList<>();

    flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(15), 3, 100));
    flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(30), 3, 144));

    return flexibleRollingPeriodKeys;
  }

  @Test
  void check400ResponseStatusFor99KeysPayloadWithDifferentDays() {
    ResponseEntity<Void> actResponse = executor.executePost(build99KeysPayload());

    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  private Collection<TemporaryExposureKey> build99KeysPayload() {
    ArrayList<TemporaryExposureKey> flexibleRollingPeriodKeys = new ArrayList<>();
    // 30 Keys for today
    generatePayloadKeys(flexibleRollingPeriodKeys, 30, 0);
    // 20 Keys for tomorrow
    generatePayloadKeys(flexibleRollingPeriodKeys, 20, -1);
    //50 Keys past days
    generatePayloadKeys(flexibleRollingPeriodKeys, 49, 4);
    return flexibleRollingPeriodKeys;
  }

  private void generatePayloadKeys(ArrayList<TemporaryExposureKey> flexibleRollingPeriodKeys, Integer numberOfKeys,
      Integer dayAgo) {
    IntStream.range(0, numberOfKeys).forEach(numberKey ->
        flexibleRollingPeriodKeys.add(buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1,
            createRollingStartIntervalNumber(dayAgo), 3, 100)));
  }
}
