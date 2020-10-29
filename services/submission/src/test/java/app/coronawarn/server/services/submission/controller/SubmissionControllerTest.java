package app.coronawarn.server.services.submission.controller;

import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.VALID_KEY_DATA_1;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.VALID_KEY_DATA_2;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildMultipleKeys;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildMultipleKeysWithoutDSOS;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildMultipleKeysWithoutDSOSAndTRL;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildMultipleKeysWithoutTRL;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildPayload;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildPayloadForOriginCountry;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildPayloadWithOneKey;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildPayloadWithPadding;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildPayloadWithTooLargePadding;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildPayloadWithVisitedCountries;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildPayloadWithoutOriginCountry;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildTemporaryExposureKey;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.createRollingStartIntervalNumber;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.OK;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.monitoring.SubmissionMonitor;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import com.google.protobuf.ByteString;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ActiveProfiles({"disable-ssl-client-verification", "disable-ssl-client-verification-verify-hostname"})
@TestInstance(Lifecycle.PER_CLASS)
class SubmissionControllerTest {

  @MockBean
  private DiagnosisKeyService diagnosisKeyService;

  @MockBean
  private TanVerifier tanVerifier;

  @MockBean
  private SubmissionMonitor submissionMonitor;

  @MockBean
  private FakeDelayManager fakeDelayManager;

  @Autowired
  private RequestExecutor executor;

  @Autowired
  private SubmissionServiceConfig config;

  private static Stream<Arguments> invalidVisitedCountries() {
    return Stream.of(
        Arguments.of(List.of("")),
        Arguments.of(List.of("D")),
        Arguments.of(List.of("FRE")),
        Arguments.of(List.of("DE", "XX")),
        Arguments.of(List.of("DE", "FRE"))
    );
  }

  private static Stream<Arguments> validVisitedCountries() {
    return Stream.of(
        Arguments.of(List.of("DE")),
        Arguments.of(List.of("DE", "FR")));
  }

  public static SubmissionPayload buildPayloadWithInvalidKey() {
    TemporaryExposureKey invalidKey =
        buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(2), 999,
            ReportType.CONFIRMED_TEST, 1);
    return buildPayload(invalidKey);
  }

  private static Stream<Arguments> createIncompleteHeaders() {
    return Stream.of(
        Arguments.of(HttpHeaderBuilder.builder().build()),
        Arguments.of(HttpHeaderBuilder.builder().contentTypeProtoBuf().build()),
        Arguments.of(HttpHeaderBuilder.builder().contentTypeProtoBuf().withoutCwaFake().build()),
        Arguments.of(HttpHeaderBuilder.builder().contentTypeProtoBuf().cwaAuth().build()));
  }

  private static Stream<Arguments> createDeniedHttpMethods() {
    return Arrays.stream(HttpMethod.values())
        .filter(method -> method != HttpMethod.POST)
        .filter(method -> method != HttpMethod.PATCH) /* not supported by Rest Template */
        .map(Arguments::of);
  }

  @BeforeEach
  public void setUpMocks() {
    when(tanVerifier.verifyTan(anyString())).thenReturn(true);
    when(fakeDelayManager.getJitteredFakeDelay()).thenReturn(1000L);
  }

  @Test
  void checkResponseStatusForValidParameters() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayload(buildMultipleKeys(config)));
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @Test
  void checkResponseStatusForValidParametersWithPadding() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithPadding(buildMultipleKeys(config)));
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @Test
  void check400ResponseStatusForInvalidKeys() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithInvalidKey());
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void singleKeyWithOutdatedRollingStartIntervalNumberDoesNotGetSaved() {
    ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    executor.executePost(buildPayload(createOutdatedKey()));

    verify(diagnosisKeyService, atLeastOnce()).saveDiagnosisKeys(argument.capture());
    assertThat(argument.getValue()).isEmpty();
  }

  @Test
  void keysWithOutdatedRollingStartIntervalNumberDoNotGetSaved() {
    Collection<TemporaryExposureKey> submittedKeys = buildMultipleKeys(config);
    TemporaryExposureKey outdatedKey = createOutdatedKey();
    submittedKeys.add(outdatedKey);
    ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    SubmissionPayload submissionPayload = buildPayload(submittedKeys);
    executor.executePost(submissionPayload);

    verify(diagnosisKeyService, atLeastOnce()).saveDiagnosisKeys(argument.capture());
    submittedKeys.remove(outdatedKey);
    assertSubmissionPayloadKeysCorrespondToEachOther(submittedKeys, argument.getValue(), submissionPayload);
  }

  @Test
  void submissionPayloadWithoutConsentIsPersistedCorrectly() {
    Collection<TemporaryExposureKey> submittedKeys = buildMultipleKeys(config);
    ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    boolean consentToFederation = false;
    SubmissionPayload submissionPayload = buildPayload(submittedKeys, consentToFederation);
    executor.executePost(submissionPayload);

    verify(diagnosisKeyService, atLeastOnce()).saveDiagnosisKeys(argument.capture());
    assertSubmissionPayloadKeysCorrespondToEachOther(submittedKeys, argument.getValue(), submissionPayload);
  }

  @Test
  void submissionPayloadWithConsentIsPersistedCorrectly() {
    Collection<TemporaryExposureKey> submittedKeys = buildMultipleKeys(config);
    ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    boolean consentToFederation = true;
    SubmissionPayload submissionPayload = buildPayload(submittedKeys, consentToFederation);
    executor.executePost(submissionPayload);

    verify(diagnosisKeyService, atLeastOnce()).saveDiagnosisKeys(argument.capture());
    assertSubmissionPayloadKeysCorrespondToEachOther(submittedKeys, argument.getValue(), submissionPayload);
  }

  @Test
  void submissionPayloadAddMissingOriginCountryAsVisitedCountry() {
    ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    SubmissionPayload submissionPayload = buildPayloadWithVisitedCountries(List.of("FR"));
    executor.executePost(submissionPayload);

    verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(argument.capture());

    assertThat(argument.getValue())
        .allMatch(savedKey -> savedKey.getVisitedCountries().contains(config.getDefaultOriginCountry()))
        .hasSize((submissionPayload.getKeysList()).size() * config.getRandomKeyPaddingMultiplier());
  }

  /**
   * The test verifies that even if the payload does not provide keys with DSOS, the information is still derived from
   * the TRL field and correctly persisted.
   *
   * <li>DSOS - days since onset of symptoms
   * <li>TRL  - transmission risk level
   */
  @Test
  void checkDSOSIsPersistedForKeysWithTRLOnly() {
    Collection<TemporaryExposureKey> submittedKeys = buildMultipleKeysWithoutDSOS(config);
    ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    SubmissionPayload submissionPayload = buildPayload(submittedKeys);
    executor.executePost(submissionPayload);

    verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(argument.capture());

    Collection<DiagnosisKey> values = argument.getValue();
    assertDSOSCorrectlyComputedFromTRL(config, submittedKeys, values);
  }

  /**
   * The test verifies that even if the payload does not provide keys with TRL, the information is still derived from
   * the DSOS field and correctly persisted.
   *
   * <li>DSOS - days since onset of symptoms
   * <li>TRL  - transmission risk level
   */
  @Test
  void checkTRLIsPersistedForKeysWithDSOSOnly() {
    Collection<TemporaryExposureKey> submittedKeys = buildMultipleKeysWithoutTRL(config);
    ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    SubmissionPayload submissionPayload = buildPayload(submittedKeys);
    executor.executePost(submissionPayload);

    verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(argument.capture());

    Collection<DiagnosisKey> values = argument.getValue();
    assertTRLCorrectlyComputedFromDSOS(config, submittedKeys, values);
  }

  /**
   * The test verifies that a payload is rejected when both TRL and DSOS are missing from a single key.
   *
   * <li>DSOS - days since onset of symptoms
   * <li>TRL  - transmission risk level
   */
  @Test
  void checkErrorIsThrownWhenKeysAreMissingDSOSAndTRL() {
    Collection<TemporaryExposureKey> submittedKeys = buildMultipleKeysWithoutDSOSAndTRL(config);
    SubmissionPayload submissionPayload = buildPayload(submittedKeys);
    ResponseEntity<Void> response = executor.executePost(submissionPayload);
    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void checkSaveOperationCallAndFakeDelayUpdateForValidParameters() {
    Collection<TemporaryExposureKey> submittedKeys = buildMultipleKeys(config);
    ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    SubmissionPayload submissionPayload = buildPayload(submittedKeys);
    executor.executePost(submissionPayload);

    verify(diagnosisKeyService, atLeastOnce()).saveDiagnosisKeys(argument.capture());
    verify(fakeDelayManager, times(1)).updateFakeRequestDelay(anyLong());
    assertSubmissionPayloadKeysCorrespondToEachOther(submittedKeys, argument.getValue(), submissionPayload);
  }

  @ParameterizedTest
  @MethodSource("createIncompleteHeaders")
  void badRequestIfCwaHeadersMissing(HttpHeaders headers) {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithOneKey(), headers);

    verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @ParameterizedTest
  @MethodSource("createDeniedHttpMethods")
  void checkOnlyPostAllowed(HttpMethod deniedHttpMethod) {
    // INTERNAL_SERVER_ERROR is the result of blocking by StrictFirewall for non POST calls.
    //                       We can change this when Spring Security 5.4.x is released.
    // METHOD_NOT_ALLOWED is the result of TRACE calls (disabled by default in tomcat)
    List<HttpStatus> allowedErrors = Arrays.asList(INTERNAL_SERVER_ERROR, FORBIDDEN, METHOD_NOT_ALLOWED);

    HttpStatus actStatus = executor.execute(deniedHttpMethod, null).getStatusCode();

    assertThat(allowedErrors)
        .withFailMessage(deniedHttpMethod + " resulted in unexpected status: " + actStatus)
        .contains(actStatus);
  }

  @Test
  void invalidTanHandling() {
    when(tanVerifier.verifyTan(anyString())).thenReturn(false);

    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithOneKey());

    verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    verify(fakeDelayManager, times(1)).updateFakeRequestDelay(anyLong());
    assertThat(actResponse.getStatusCode()).isEqualTo(FORBIDDEN);
  }

  @Test
  void testInvalidPaddingSubmissionPayload() {
    ResponseEntity<Void> actResponse = executor
        .executePost(buildPayloadWithTooLargePadding(config, buildMultipleKeys(config)));
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void testInvalidOriginCountrySubmissionPayload() {
    ResponseEntity<Void> actResponse = executor
        .executePost(buildPayloadForOriginCountry(buildMultipleKeys(config), "IT"));
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void testEmptyOriginCountrySubmissionPayload() {
    ResponseEntity<Void> actResponse = executor
        .executePost(buildPayloadForOriginCountry(buildMultipleKeys(config), ""));
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @Test
  void testMissingOriginCountrySubmissionPayload() {
    ResponseEntity<Void> actResponse = executor
        .executePost(buildPayloadWithoutOriginCountry(buildMultipleKeys(config)));
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @ParameterizedTest
  @MethodSource("invalidVisitedCountries")
  void testInvalidVisitedCountriesSubmissionPayload(List<String> visitedCountries) {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithVisitedCountries(visitedCountries));
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @ParameterizedTest
  @MethodSource("validVisitedCountries")
  void testValidVisitedCountriesSubmissionPayload(List<String> visitedCountries) {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithVisitedCountries(visitedCountries));
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @Test
  void checkRealRequestHandlingIsMonitored() {
    executor.executePost(buildPayloadWithOneKey());

    verify(submissionMonitor, times(1)).incrementRequestCounter();
    verify(submissionMonitor, times(1)).incrementRealRequestCounter();
    verify(submissionMonitor, never()).incrementFakeRequestCounter();
    verify(submissionMonitor, never()).incrementInvalidTanRequestCounter();
  }

  @Test
  void checkInvalidTanHandlingIsMonitored() {
    when(tanVerifier.verifyTan(anyString())).thenReturn(false);

    executor.executePost(buildPayloadWithOneKey());

    verify(submissionMonitor, times(1)).incrementRequestCounter();
    verify(submissionMonitor, times(1)).incrementRealRequestCounter();
    verify(submissionMonitor, never()).incrementFakeRequestCounter();
    verify(submissionMonitor, times(1)).incrementInvalidTanRequestCounter();
  }

  private TemporaryExposureKey createOutdatedKey() {
    return TemporaryExposureKey.newBuilder()
        .setKeyData(ByteString.copyFromUtf8(VALID_KEY_DATA_2))
        .setRollingStartIntervalNumber(createRollingStartIntervalNumber(config.getRetentionDays()))
        .setRollingPeriod(DiagnosisKey.MAX_ROLLING_PERIOD)
        .setTransmissionRiskLevel(5).build();
  }

  private void assertSubmissionPayloadKeysCorrespondToEachOther(
      Collection<TemporaryExposureKey> submittedTemporaryExposureKeys,
      Collection<DiagnosisKey> savedDiagnosisKeys,
      SubmissionPayload submissionPayload) {

    Set<DiagnosisKey> submittedDiagnosisKeys = submittedTemporaryExposureKeys.stream()
        .map(submittedTemporaryExposureKey -> DiagnosisKey
            .builder()
            .fromTemporaryExposureKeyAndMetadata(submittedTemporaryExposureKey,
                submissionPayload.getVisitedCountriesList(),
                submissionPayload.getOrigin(),
                submissionPayload.getConsentToFederation())
            .withConsentToFederation(submissionPayload.getConsentToFederation())
            .withVisitedCountries(new HashSet<>(submissionPayload.getVisitedCountriesList()))
            .withCountryCode(defaultIfBlank(submissionPayload.getOrigin(), config.getDefaultOriginCountry()))
            .build())
        .collect(Collectors.toSet());

    assertThat(savedDiagnosisKeys).hasSize(submittedDiagnosisKeys.size() * config.getRandomKeyPaddingMultiplier());
    assertThat(savedDiagnosisKeys).containsAll(submittedDiagnosisKeys);

    submittedDiagnosisKeys.forEach(submittedDiagnosisKey -> {
      List<DiagnosisKey> savedKeysForSingleSubmittedKey = savedDiagnosisKeys.stream()
          .filter(savedDiagnosisKey -> savedDiagnosisKey.getRollingPeriod() ==
              submittedDiagnosisKey.getRollingPeriod())
          .filter(savedDiagnosisKey -> savedDiagnosisKey.getTransmissionRiskLevel() ==
              submittedDiagnosisKey.getTransmissionRiskLevel())
          .filter(savedDiagnosisKey -> savedDiagnosisKey.getRollingStartIntervalNumber() ==
              submittedDiagnosisKey.getRollingStartIntervalNumber())
          .collect(Collectors.toList());

      assertThat(savedKeysForSingleSubmittedKey).hasSize(config.getRandomKeyPaddingMultiplier());
      assertThat(savedKeysForSingleSubmittedKey.stream().filter(savedKey ->
          Arrays.equals(savedKey.getKeyData(), submittedDiagnosisKey.getKeyData()))).hasSize(1);
      assertThat(savedKeysForSingleSubmittedKey).allMatch(
          savedKey -> savedKey.getRollingPeriod() == submittedDiagnosisKey.getRollingPeriod());
      assertThat(savedKeysForSingleSubmittedKey).allMatch(
          savedKey -> savedKey.getRollingStartIntervalNumber() == submittedDiagnosisKey
              .getRollingStartIntervalNumber());
      assertThat(savedKeysForSingleSubmittedKey).allMatch(
          savedKey -> savedKey.getTransmissionRiskLevel() == submittedDiagnosisKey.getTransmissionRiskLevel());
    });
  }

  private void assertTRLCorrectlyComputedFromDSOS(SubmissionServiceConfig config,
      Collection<TemporaryExposureKey> submittedTEKs, Collection<DiagnosisKey> diagnosisKeys) {
    submittedTEKs.stream().map(tek -> Pair.of(tek, findDiagnosisKeyMatch(tek, diagnosisKeys))).forEach(pair -> {
      int tekDSOS = pair.getLeft().getDaysSinceOnsetOfSymptoms();
      int dkTRL = pair.getRight().getTransmissionRiskLevel();
      Integer expectedTRL = config.getTekFieldDerivations().deriveTransmissionRiskLevelFromDaysSinceSymptoms(tekDSOS);
      Assertions.assertEquals(expectedTRL, dkTRL);
    });
  }

  private void assertDSOSCorrectlyComputedFromTRL(SubmissionServiceConfig config,
      Collection<TemporaryExposureKey> submittedTEKs, Collection<DiagnosisKey> diagnosisKeys) {
    submittedTEKs.stream().map(tek -> Pair.of(tek, findDiagnosisKeyMatch(tek, diagnosisKeys))).forEach(pair -> {
      int tekTRL = pair.getLeft().getTransmissionRiskLevel();
      int dkDSOS = pair.getRight().getDaysSinceOnsetOfSymptoms();
      Integer expectedDsos = config.getTekFieldDerivations().deriveDaysSinceSymptomsFromTransmissionRiskLevel(tekTRL);
      Assertions.assertEquals(expectedDsos, dkDSOS);
    });
  }

  private DiagnosisKey findDiagnosisKeyMatch(TemporaryExposureKey temporaryExposureKey,
      Collection<DiagnosisKey> diagnosisKeys) {
    return diagnosisKeys
        .stream()
        .filter(
            diagnosisKey -> temporaryExposureKey.getKeyData().equals(ByteString.copyFrom(diagnosisKey.getKeyData())))
        .findFirst().orElseThrow();
  }
}
