package app.coronawarn.server.services.submission.controller;

import static app.coronawarn.server.common.persistence.service.utils.checkins.CheckinsDateSpecification.TEN_MINUTE_INTERVAL_DERIVATION;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.VALID_KEY_DATA_1;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.VALID_KEY_DATA_2;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildMultipleKeys;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildMultipleKeysWithoutDSOS;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildMultipleKeysWithoutDSOSAndTRL;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildMultipleKeysWithoutTRL;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildPayload;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildPayloadForOriginCountry;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildPayloadWithCheckinData;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildPayloadWithOneKey;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildPayloadWithPadding;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildPayloadWithTooLargePadding;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildPayloadWithVisitedCountries;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildPayloadWithoutOriginCountry;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildTemporaryExposureKey;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.createRollingStartIntervalNumber;
import static java.time.ZoneOffset.UTC;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
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
import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.repository.TraceTimeIntervalWarningRepository;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import app.coronawarn.server.services.submission.checkins.EventCheckinDataValidatorTest;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.monitoring.SubmissionMonitor;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import com.google.protobuf.ByteString;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.data.Index;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@TestInstance(Lifecycle.PER_CLASS)
@SuppressWarnings("unchecked")
class SubmissionControllerTest {

  @MockBean
  private DiagnosisKeyService diagnosisKeyService;

  @MockBean
  private SubmissionMonitor submissionMonitor;

  @MockBean
  private FakeDelayManager fakeDelayManager;


  @MockBean
  private TanVerifier tanVerifier;

  @Autowired
  private RequestExecutor executor;

  @Autowired
  private SubmissionServiceConfig config;

  @Autowired
  private TraceTimeIntervalWarningRepository traceTimeIntervalWarningRepository;


  @BeforeEach
  public void setUpMocks() {
    traceTimeIntervalWarningRepository.deleteAll();
    when(tanVerifier.verifyTan(anyString())).thenReturn(true);
    when(fakeDelayManager.getJitteredFakeDelay()).thenReturn(1000L);
  }


  private void assertDSOSCorrectlyComputedFromTRL(final SubmissionServiceConfig config,
      final Collection<TemporaryExposureKey> submittedTEKs, final Collection<DiagnosisKey> diagnosisKeys) {
    submittedTEKs.stream().map(tek -> Pair.of(tek, findDiagnosisKeyMatch(tek, diagnosisKeys))).forEach(pair -> {
      final int tekTRL = pair.getLeft().getTransmissionRiskLevel();
      final int dkDSOS = pair.getRight().getDaysSinceOnsetOfSymptoms();
      final Integer expectedDsos = config.getTekFieldDerivations()
          .deriveDaysSinceSymptomsFromTransmissionRiskLevel(tekTRL);
      Assertions.assertEquals(expectedDsos, dkDSOS);
    });
  }

  private void assertSubmissionPayloadKeysCorrespondToEachOther(
      final Collection<TemporaryExposureKey> submittedTemporaryExposureKeys,
      final Collection<DiagnosisKey> savedDiagnosisKeys, final SubmissionPayload submissionPayload) {

    final Set<DiagnosisKey> submittedDiagnosisKeys = submittedTemporaryExposureKeys.stream()
        .map(submittedTemporaryExposureKey -> DiagnosisKey.builder()
            .fromTemporaryExposureKeyAndMetadata(submittedTemporaryExposureKey,
                SubmissionType.SUBMISSION_TYPE_PCR_TEST, submissionPayload.getVisitedCountriesList(),
                submissionPayload.getOrigin(), submissionPayload.getConsentToFederation())
            .withConsentToFederation(submissionPayload.getConsentToFederation())
            .withVisitedCountries(new HashSet<>(submissionPayload.getVisitedCountriesList()))
            .withCountryCode(defaultIfBlank(submissionPayload.getOrigin(), config.getDefaultOriginCountry())).build())
        .collect(Collectors.toSet());

    assertThat(savedDiagnosisKeys).hasSize(submittedDiagnosisKeys.size() * config.getRandomKeyPaddingMultiplier());
    assertThat(savedDiagnosisKeys).containsAll(submittedDiagnosisKeys);

    submittedDiagnosisKeys.forEach(submittedDiagnosisKey -> {
      final List<DiagnosisKey> savedKeysForSingleSubmittedKey = savedDiagnosisKeys.stream()
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

  private void assertTraceWarningsHaveBeenSaved(final int numberOfExpectedWarningsSaved) {
    final List<TraceTimeIntervalWarning> storedTimeIntervalWarnings = StreamSupport
        .stream(traceTimeIntervalWarningRepository.findAll().spliterator(), false).collect(Collectors.toList());
    assertEquals(numberOfExpectedWarningsSaved, storedTimeIntervalWarnings.size());
  }

  private void assertTRLCorrectlyComputedFromDSOS(final SubmissionServiceConfig config,
      final Collection<TemporaryExposureKey> submittedTEKs, final Collection<DiagnosisKey> diagnosisKeys) {
    submittedTEKs.stream().map(tek -> Pair.of(tek, findDiagnosisKeyMatch(tek, diagnosisKeys))).forEach(pair -> {
      final int tekDSOS = pair.getLeft().getDaysSinceOnsetOfSymptoms();
      final int dkTRL = pair.getRight().getTransmissionRiskLevel();
      final Integer expectedTRL = config.getTekFieldDerivations()
          .deriveTransmissionRiskLevelFromDaysSinceSymptoms(tekDSOS);
      Assertions.assertEquals(expectedTRL, dkTRL);
    });
  }

  private TemporaryExposureKey createOutdatedKey() {
    return TemporaryExposureKey.newBuilder().setKeyData(ByteString.copyFromUtf8(VALID_KEY_DATA_2))
        .setRollingStartIntervalNumber(createRollingStartIntervalNumber(config.getRetentionDays() + 1))
        .setRollingPeriod(DiagnosisKey.MAX_ROLLING_PERIOD).setTransmissionRiskLevel(5).build();
  }

  private DiagnosisKey findDiagnosisKeyMatch(final TemporaryExposureKey temporaryExposureKey,
      final Collection<DiagnosisKey> diagnosisKeys) {
    return diagnosisKeys.stream()
        .filter(
            diagnosisKey -> temporaryExposureKey.getKeyData().equals(ByteString.copyFrom(diagnosisKey.getKeyData())))
        .findFirst().orElseThrow();
  }


  @ParameterizedTest
  @MethodSource("createIncompleteHeaders")
  void badRequestIfCwaHeadersMissing(final HttpHeaders headers) {
    final ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithOneKey(), headers);

    verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void check400ResponseStatusForInvalidKeys() {
    final ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithInvalidKey());
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  /**
   * The test verifies that even if the payload does not provide keys with DSOS, the information is still derived from
   * the TRL field and correctly persisted.
   *
   * <li>DSOS - days since onset of symptoms
   * <li>TRL - transmission risk level
   */
  @Test
  void checkDSOSIsPersistedForKeysWithTRLOnly() {
    final Collection<TemporaryExposureKey> submittedKeys = buildMultipleKeysWithoutDSOS(config);
    final ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    final SubmissionPayload submissionPayload = buildPayload(submittedKeys);
    executor.executePost(submissionPayload);

    verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(argument.capture());

    final Collection<DiagnosisKey> values = argument.getValue();
    assertDSOSCorrectlyComputedFromTRL(config, submittedKeys, values);
  }

  /**
   * The test verifies that a payload is rejected when both TRL and DSOS are missing from a single key.
   *
   * <li>DSOS - days since onset of symptoms
   * <li>TRL - transmission risk level
   */
  @Test
  void checkErrorIsThrownWhenKeysAreMissingDSOSAndTRL() {
    final Collection<TemporaryExposureKey> submittedKeys = buildMultipleKeysWithoutDSOSAndTRL(config);
    final SubmissionPayload submissionPayload = buildPayload(submittedKeys);
    final ResponseEntity<Void> response = executor.executePost(submissionPayload);
    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
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

  @ParameterizedTest
  @MethodSource("createDeniedHttpMethods")
  void checkOnlyPostAllowed(final HttpMethod deniedHttpMethod) {
    // INTERNAL_SERVER_ERROR is the result of blocking by StrictFirewall for non POST calls.
    // We can change this when Spring Security 5.4.x is released.
    // METHOD_NOT_ALLOWED is the result of TRACE calls (disabled by default in tomcat)
    final List<HttpStatus> allowedErrors = Arrays.asList(INTERNAL_SERVER_ERROR, FORBIDDEN, METHOD_NOT_ALLOWED);

    final HttpStatus actStatus = executor.execute(deniedHttpMethod, null).getStatusCode();

    assertThat(allowedErrors).withFailMessage(deniedHttpMethod + " resulted in unexpected status: " + actStatus)
        .contains(actStatus);
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
  void checkResponseStatusForValidParameters() {
    final ResponseEntity<Void> actResponse = executor.executePost(buildPayload(buildMultipleKeys(config)));
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @Test
  void checkResponseStatusForValidParametersWithPadding() {
    final ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithPadding(buildMultipleKeys(config)));
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @Test
  void checkSaveOperationCallAndFakeDelayUpdateForValidParameters() {
    final Collection<TemporaryExposureKey> submittedKeys = buildMultipleKeys(config);
    final ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    final SubmissionPayload submissionPayload = buildPayload(submittedKeys);
    executor.executePost(submissionPayload);

    verify(diagnosisKeyService, atLeastOnce()).saveDiagnosisKeys(argument.capture());
    verify(fakeDelayManager, times(1)).updateFakeRequestDelay(anyLong());
    assertSubmissionPayloadKeysCorrespondToEachOther(submittedKeys, argument.getValue(), submissionPayload);
  }

  /**
   * The test verifies that even if the payload does not provide keys with TRL, the information is still derived from
   * the DSOS field and correctly persisted.
   *
   * <li>DSOS - days since onset of symptoms
   * <li>TRL - transmission risk level
   */
  @Test
  void checkTRLIsPersistedForKeysWithDSOSOnly() {
    final Collection<TemporaryExposureKey> submittedKeys = buildMultipleKeysWithoutTRL(config);
    final ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    final SubmissionPayload submissionPayload = buildPayload(submittedKeys);
    executor.executePost(submissionPayload);

    verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(argument.capture());

    final Collection<DiagnosisKey> values = argument.getValue();
    assertTRLCorrectlyComputedFromDSOS(config, submittedKeys, values);
  }

  @Test
  void invalidTanHandling() {
    when(tanVerifier.verifyTan(anyString())).thenReturn(false);

    final ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithOneKey());

    verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    verify(fakeDelayManager, times(1)).updateFakeRequestDelay(anyLong());
    assertThat(actResponse.getStatusCode()).isEqualTo(FORBIDDEN);
  }

  @Test
  void keysWithOutdatedRollingStartIntervalNumberDoNotGetSaved() {
    final Collection<TemporaryExposureKey> submittedKeys = buildMultipleKeys(config);
    final TemporaryExposureKey outdatedKey = createOutdatedKey();
    submittedKeys.add(outdatedKey);
    final ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    final SubmissionPayload submissionPayload = buildPayload(submittedKeys);
    executor.executePost(submissionPayload);

    verify(diagnosisKeyService, atLeastOnce()).saveDiagnosisKeys(argument.capture());
    submittedKeys.remove(outdatedKey);
    assertSubmissionPayloadKeysCorrespondToEachOther(submittedKeys, argument.getValue(), submissionPayload);
  }

  @Test
  void singleKeyWithOutdatedRollingStartIntervalNumberDoesNotGetSaved() {
    final ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    executor.executePost(buildPayload(createOutdatedKey()));

    verify(diagnosisKeyService, atLeastOnce()).saveDiagnosisKeys(argument.capture());
    assertThat(argument.getValue()).isEmpty();
  }

  @Test
  void submissionPayloadAddMissingOriginCountryAsVisitedCountry() {
    final ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    final SubmissionPayload submissionPayload = buildPayloadWithVisitedCountries(List.of("FR"));
    executor.executePost(submissionPayload);

    verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(argument.capture());

    assertThat(argument.getValue())
        .allMatch(savedKey -> savedKey.getVisitedCountries().contains(config.getDefaultOriginCountry()))
        .hasSize(submissionPayload.getKeysList().size() * config.getRandomKeyPaddingMultiplier());
  }

  @Test
  void submissionPayloadWithConsentIsPersistedCorrectly() {
    final Collection<TemporaryExposureKey> submittedKeys = buildMultipleKeys(config);
    final ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    final boolean consentToFederation = true;
    final SubmissionPayload submissionPayload = buildPayload(submittedKeys, consentToFederation);
    executor.executePost(submissionPayload);

    verify(diagnosisKeyService, atLeastOnce()).saveDiagnosisKeys(argument.capture());
    assertSubmissionPayloadKeysCorrespondToEachOther(submittedKeys, argument.getValue(), submissionPayload);
  }

  @Test
  void submissionPayloadWithoutConsentIsPersistedCorrectly() {
    final Collection<TemporaryExposureKey> submittedKeys = buildMultipleKeys(config);
    final ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    final boolean consentToFederation = false;
    final SubmissionPayload submissionPayload = buildPayload(submittedKeys, consentToFederation);
    executor.executePost(submissionPayload);

    verify(diagnosisKeyService, atLeastOnce()).saveDiagnosisKeys(argument.capture());
    assertSubmissionPayloadKeysCorrespondToEachOther(submittedKeys, argument.getValue(), submissionPayload);
  }

  @Test
  void testCheckinDataIsFilteredForFutureEvents() {
    final Instant thisInstant = Instant.now();
    final long eventCheckinInTheFuture = LocalDateTime.ofInstant(thisInstant, UTC).plusMinutes(11).toEpochSecond(UTC);
    final long eventCheckoutInTheFuture = LocalDateTime.ofInstant(thisInstant, UTC).plusMinutes(20)
        .toEpochSecond(UTC);

    final List<CheckIn> checkins = List
        .of(CheckIn.newBuilder().setStartIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInTheFuture))
            .setEndIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckoutInTheFuture))
            .setTransmissionRiskLevel(3).setLocationId(EventCheckinDataValidatorTest.CORRECT_LOCATION_ID).build());

    final ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithCheckinData(checkins));
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
    assertTraceWarningsHaveBeenSaved(0);
  }

  @Test
  void testCheckinDataIsFilteredForOldEvents() {
    final Integer daysInThePast = config.getAcceptedEventDateThresholdDays() + 1;
    final Instant thisInstant = Instant.now();
    final long eventCheckoutInThePast = LocalDateTime.ofInstant(thisInstant, UTC).minusDays(daysInThePast)
        .toEpochSecond(UTC);
    final long eventCheckinInThePast = LocalDateTime.ofInstant(thisInstant, UTC).minusDays(daysInThePast + 1)
        .toEpochSecond(UTC);

    final List<CheckIn> checkins = List
        .of(CheckIn.newBuilder().setStartIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInThePast))
            .setEndIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckoutInThePast))
            .setTransmissionRiskLevel(1).setLocationId(EventCheckinDataValidatorTest.CORRECT_LOCATION_ID).build());

    final ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithCheckinData(checkins));
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
    assertTraceWarningsHaveBeenSaved(0);
  }

  @Test
  void testCheckinDataHeadersAreCorrectlyFilled() {
    final Integer daysInThePast = config.getAcceptedEventDateThresholdDays() + 1;
    final Instant thisInstant = Instant.now();
    final long eventCheckoutInThePast = LocalDateTime.ofInstant(thisInstant, UTC).minusDays(daysInThePast)
        .toEpochSecond(UTC);
    final long eventCheckinInThePast = LocalDateTime.ofInstant(thisInstant, UTC).minusDays(daysInThePast + 1)
        .toEpochSecond(UTC);

    final long eventCheckinInAllowedPeriod = LocalDateTime.ofInstant(Instant.now(), UTC).minusDays(10)
        .toEpochSecond(UTC);

    final List<CheckIn> checkins = List
        .of(CheckIn.newBuilder().setStartIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInThePast))
                .setEndIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckoutInThePast))
                .setTransmissionRiskLevel(1).setLocationId(EventCheckinDataValidatorTest.CORRECT_LOCATION_ID).build(),
            CheckIn.newBuilder().setTransmissionRiskLevel(3)
                .setStartIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInAllowedPeriod))
                .setEndIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInAllowedPeriod) + 10)
                .setLocationId(EventCheckinDataValidatorTest.CORRECT_LOCATION_ID).build());

    final ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithCheckinData(checkins));
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
    assertThat(actResponse.getHeaders().get("cwa-filtered-checkins")).contains("1", Index.atIndex(0));
    assertThat(actResponse.getHeaders().get("cwa-saved-checkins")).contains("2", Index.atIndex(0));
  }

  @Test
  void testCheckinDataIsFilteredForTransmissionRiskLevel() {
    final long eventCheckinInThePast = LocalDateTime.ofInstant(Instant.now(), UTC).minusDays(10).toEpochSecond(UTC);

    // both trls below are mapped to zero in the persistence/trl-value-mapping.yaml
    final List<CheckIn> invalidCheckinData = List.of(
        CheckIn.newBuilder().setTransmissionRiskLevel(1)
            .setStartIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInThePast))
            .setEndIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInThePast) + 10)
            .setLocationId(EventCheckinDataValidatorTest.CORRECT_LOCATION_ID).build(),
        CheckIn.newBuilder().setTransmissionRiskLevel(2)
            .setStartIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInThePast) + 11)
            .setEndIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInThePast) + 22)
            .setLocationId(EventCheckinDataValidatorTest.CORRECT_LOCATION_ID).build());

    final ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithCheckinData(invalidCheckinData));
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
    assertTraceWarningsHaveBeenSaved(0);
  }

  @Test
  void testEmptyOriginCountrySubmissionPayload() {
    final ResponseEntity<Void> actResponse = executor
        .executePost(buildPayloadForOriginCountry(buildMultipleKeys(config), ""));
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @Test
  void testInvalidCheckOutTime() {
    final List<CheckIn> invalidCheckinData = List.of(
        CheckIn.newBuilder().setTransmissionRiskLevel(2).setStartIntervalNumber(4).setEndIntervalNumber(3).build());

    final ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithCheckinData(invalidCheckinData));
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
    assertTraceWarningsHaveBeenSaved(0);
  }

  @Test
  void testInvalidOriginCountrySubmissionPayload() {
    final ResponseEntity<Void> actResponse = executor
        .executePost(buildPayloadForOriginCountry(buildMultipleKeys(config), "IT"));
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void testInvalidPaddingSubmissionPayload() {
    final ResponseEntity<Void> actResponse = executor
        .executePost(buildPayloadWithTooLargePadding(config, buildMultipleKeys(config)));
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void testInvalidTransmissionRiskLevelInCheckinData() {
    final List<CheckIn> invalidCheckinData = List.of(
        CheckIn.newBuilder().setTransmissionRiskLevel(0).setStartIntervalNumber(1).setEndIntervalNumber(2).build(),
        CheckIn.newBuilder().setTransmissionRiskLevel(4).setStartIntervalNumber(1).setEndIntervalNumber(1).build());

    final ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithCheckinData(invalidCheckinData));
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
    assertTraceWarningsHaveBeenSaved(0);
  }

  @ParameterizedTest
  @MethodSource("invalidVisitedCountries")
  void testInvalidVisitedCountriesSubmissionPayload(final List<String> visitedCountries) {
    final ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithVisitedCountries(visitedCountries));
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void testMissingOriginCountrySubmissionPayload() {
    final ResponseEntity<Void> actResponse = executor
        .executePost(buildPayloadWithoutOriginCountry(buildMultipleKeys(config)));
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @Test
  void testValidCheckinData() {

    final long eventCheckinInThePast = LocalDateTime.ofInstant(Instant.now(), UTC).minusDays(10).toEpochSecond(UTC);

    final List<CheckIn> validCheckinData = List.of(
        CheckIn.newBuilder().setTransmissionRiskLevel(3)
            .setStartIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInThePast))
            .setEndIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInThePast))
            .setLocationId(EventCheckinDataValidatorTest.CORRECT_LOCATION_ID).build(),
        CheckIn.newBuilder().setTransmissionRiskLevel(3)
            .setStartIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInThePast))
            .setEndIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInThePast) + 1)
            .setLocationId(EventCheckinDataValidatorTest.CORRECT_LOCATION_ID).build(),
        CheckIn.newBuilder().setTransmissionRiskLevel(3)
            .setStartIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInThePast))
            .setEndIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInThePast) + 10)
            .setLocationId(EventCheckinDataValidatorTest.CORRECT_LOCATION_ID).build(),
        CheckIn.newBuilder().setTransmissionRiskLevel(3)
            .setStartIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInThePast) + 11)
            .setEndIntervalNumber(TEN_MINUTE_INTERVAL_DERIVATION.apply(eventCheckinInThePast) + 22)
            .setLocationId(EventCheckinDataValidatorTest.CORRECT_LOCATION_ID).build());

    final ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithCheckinData(validCheckinData));
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
    assertTraceWarningsHaveBeenSaved(validCheckinData.size()
        + validCheckinData.size() * config.getRandomCheckinsPaddingMultiplier());
  }

  @ParameterizedTest
  @MethodSource("validVisitedCountries")
  void testValidVisitedCountriesSubmissionPayload(final List<String> visitedCountries) {
    final ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithVisitedCountries(visitedCountries));
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }


  public static SubmissionPayload buildPayloadWithInvalidKey() {
    final TemporaryExposureKey invalidKey = buildTemporaryExposureKey(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(2), 999, ReportType.CONFIRMED_TEST, 1);
    return buildPayload(invalidKey);
  }

  private static Stream<Arguments> createDeniedHttpMethods() {
    return Arrays.stream(HttpMethod.values()).filter(method -> method != HttpMethod.POST)
        .filter(method -> method != HttpMethod.PATCH) /* not supported by Rest Template */
        .map(Arguments::of);
  }

  private static Stream<Arguments> createIncompleteHeaders() {
    return Stream.of(Arguments.of(HttpHeaderBuilder.builder().build()),
        Arguments.of(HttpHeaderBuilder.builder().contentTypeProtoBuf().build()),
        Arguments.of(HttpHeaderBuilder.builder().contentTypeProtoBuf().withoutCwaFake().build()),
        Arguments.of(HttpHeaderBuilder.builder().contentTypeProtoBuf().cwaAuth().build()));
  }

  private static Stream<Arguments> invalidVisitedCountries() {
    return Stream.of(Arguments.of(List.of("")), Arguments.of(List.of("D")), Arguments.of(List.of("FRE")),
        Arguments.of(List.of("DE", "XX")), Arguments.of(List.of("DE", "FRE")));
  }

  private static Stream<Arguments> validVisitedCountries() {
    return Stream.of(Arguments.of(List.of("DE")), Arguments.of(List.of("DE", "FR")));
  }
}
