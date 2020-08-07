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
import static app.coronawarn.server.services.submission.controller.RequestExecutor.buildPayloadWithOneKey;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.buildTemporaryExposureKey;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.createRollingStartIntervalNumber;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
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
import static app.coronawarn.server.services.submission.assertions.SubmissionAssertions.*;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.monitoring.SubmissionMonitor;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import com.google.protobuf.ByteString;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

  @BeforeEach
  public void setUpMocks() {
    when(tanVerifier.verifyTan(anyString())).thenReturn(true);
    when(fakeDelayManager.getJitteredFakeDelay()).thenReturn(1000L);
  }

  @Test
  void checkResponseStatusForValidParameters() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayload(buildMultipleKeys()));
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @Test
  void checkResponseStatusForValidParametersWithPadding() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithPadding());
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @ParameterizedTest
  @MethodSource({"buildPayloadWithInvalidKeys"})
  void check400ResponseStatusForInvalidKeys(SubmissionPayload invalidPayload ) {
    ResponseEntity<Void> actResponse = executor.executePost(invalidPayload);
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
    Collection<TemporaryExposureKey> submittedKeys = buildMultipleKeys();
    TemporaryExposureKey outdatedKey = createOutdatedKey();
    submittedKeys.add(outdatedKey);
    ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    executor.executePost(buildPayload(submittedKeys));

    verify(diagnosisKeyService, atLeastOnce()).saveDiagnosisKeys(argument.capture());
    submittedKeys.remove(outdatedKey);
    assertElementsCorrespondToEachOther(submittedKeys, argument.getValue(), config);
  }

  @Test
  void checkSaveOperationCallAndFakeDelayUpdateForValidParameters() {
    Collection<TemporaryExposureKey> submittedKeys = buildMultipleKeys();
    ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    executor.executePost(buildPayload(submittedKeys));

    verify(diagnosisKeyService, atLeastOnce()).saveDiagnosisKeys(argument.capture());
    verify(fakeDelayManager, times(1)).updateFakeRequestDelay(anyLong());
    assertElementsCorrespondToEachOther(submittedKeys, argument.getValue(), config);
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
  void invalidSubmissionPayload() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithTooLargePadding());
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
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

  private SubmissionPayload buildPayload(TemporaryExposureKey key) {
    Collection<TemporaryExposureKey> keys = Stream.of(key).collect(Collectors.toCollection(ArrayList::new));
    return buildPayload(keys);
  }

  private SubmissionPayload buildPayload(Collection<TemporaryExposureKey> keys) {
    return SubmissionPayload.newBuilder()
        .addAllKeys(keys)
        .build();
  }

  private SubmissionPayload buildPayloadWithPadding() {
    return SubmissionPayload.newBuilder()
        .addAllKeys(buildMultipleKeys())
        .setPadding(ByteString.copyFrom("PaddingString".getBytes()))
        .build();
  }

  private SubmissionPayload buildPayloadWithTooLargePadding() {
    int exceedingSize = (int) (2 * config.getMaximumRequestSize().toBytes());
    byte[] bytes = new byte[exceedingSize];

    return SubmissionPayload.newBuilder()
        .addAllKeys(buildMultipleKeys())
        .setPadding(ByteString.copyFrom(bytes))
        .build();
  }

  private Collection<TemporaryExposureKey> buildMultipleKeys() {
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(config.getRetentionDays() - 1);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + DiagnosisKey.EXPECTED_ROLLING_PERIOD;
    int rollingStartIntervalNumber3 = rollingStartIntervalNumber2 + DiagnosisKey.EXPECTED_ROLLING_PERIOD;
    return Stream.of(
        buildTemporaryExposureKey(VALID_KEY_DATA_1, rollingStartIntervalNumber1, 3),
        buildTemporaryExposureKey(VALID_KEY_DATA_2, rollingStartIntervalNumber3, 6),
        buildTemporaryExposureKey(VALID_KEY_DATA_3, rollingStartIntervalNumber2, 8))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private TemporaryExposureKey createOutdatedKey() {
    return TemporaryExposureKey.newBuilder()
        .setKeyData(ByteString.copyFromUtf8(VALID_KEY_DATA_2))
        .setRollingStartIntervalNumber(createRollingStartIntervalNumber(config.getRetentionDays()))
        .setRollingPeriod(DiagnosisKey.EXPECTED_ROLLING_PERIOD)
        .setTransmissionRiskLevel(5).build();
  }

  private Stream<Arguments> buildPayloadWithInvalidKeys() {
    return Stream.of(
          Arguments.of(buildPayloadWithInvalidTransmissionRiskLevel()),
          Arguments.of(buildPayloadWithNegativeRollingStartInterval()),
          Arguments.of(buildPayloadWithFutureRollingStartInterval()),
          Arguments.of(buildPayloadWithInvalidKeyData())
          );
  }

  private SubmissionPayload buildPayloadWithInvalidTransmissionRiskLevel() {
    TemporaryExposureKey invalidKey = buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(2), 999);
    return buildPayload(invalidKey);
  }

  private SubmissionPayload buildPayloadWithNegativeRollingStartInterval() {
    TemporaryExposureKey invalidKey = buildTemporaryExposureKey(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(-1), 5);
    return buildPayload(invalidKey);
  }

  private SubmissionPayload buildPayloadWithFutureRollingStartInterval() {
    int currentPeriod = Math.toIntExact(LocalDateTime.ofInstant(Instant.now(), UTC).toEpochSecond(UTC) / 600L);
    TemporaryExposureKey invalidKey = buildTemporaryExposureKey(VALID_KEY_DATA_1,
        createRollingStartIntervalNumber(currentPeriod), 5);
    return buildPayload(invalidKey);
  }

  private SubmissionPayload buildPayloadWithInvalidKeyData() {
    TemporaryExposureKey invalidKey = buildTemporaryExposureKey("  ", createRollingStartIntervalNumber(2), 999);
    return buildPayload(invalidKey);
  }

  private static Stream<Arguments> createDeniedHttpMethods() {
    return Arrays.stream(HttpMethod.values())
        .filter(method -> method != HttpMethod.POST)
        .filter(method -> method != HttpMethod.PATCH) /* not supported by Rest Template */
        .map(Arguments::of);
  }

  private static Stream<Arguments> createIncompleteHeaders() {
    return Stream.of(
        Arguments.of(HttpHeaderBuilder.builder().build()),
        Arguments.of(HttpHeaderBuilder.builder().contentTypeProtoBuf().build()),
        Arguments.of(HttpHeaderBuilder.builder().contentTypeProtoBuf().withoutCwaFake().build()),
        Arguments.of(HttpHeaderBuilder.builder().contentTypeProtoBuf().cwaAuth().build()));
  }
}
