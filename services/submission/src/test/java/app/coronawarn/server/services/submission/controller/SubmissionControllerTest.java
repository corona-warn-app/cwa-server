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
import static app.coronawarn.server.services.submission.controller.RequestExecutor.setContentTypeProtoBufHeader;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.setCwaAuthHeader;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.setCwaFakeHeader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.OK;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import com.google.protobuf.ByteString;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmissionControllerTest {

  private static final URI SUBMISSION_URL = URI.create("/version/v1/diagnosis-keys");

  @MockBean
  private DiagnosisKeyService diagnosisKeyService;

  @MockBean
  private TanVerifier tanVerifier;

  @Autowired
  private TestRestTemplate testRestTemplate;

  @Autowired
  private RequestExecutor executor;

  @Autowired
  private SubmissionServiceConfig config;

  @BeforeEach
  public void setUpMocks() {
    when(tanVerifier.verifyTan(anyString())).thenReturn(true);
  }

  @Test
  void checkResponseStatusForValidParameters() {
    ResponseEntity<Void> actResponse = executor.executeRequest(buildPayloadWithMultipleKeys(), buildOkHeaders());
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @Test
  void check400ResponseStatusForInvalidParameters() {
    ResponseEntity<Void> actResponse = executor.executeRequest(buildPayloadWithInvalidKey(), buildOkHeaders());
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void singleKeyWithOutdatedRollingStartIntervalNumberDoesNotGetSaved() {
    Collection<TemporaryExposureKey> keys = buildPayloadWithSingleOutdatedKey();
    ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    executor.executeRequest(keys, buildOkHeaders());

    verify(diagnosisKeyService, atLeastOnce()).saveDiagnosisKeys(argument.capture());
    assertThat(argument.getValue()).isEmpty();
  }

  @Test
  void keysWithOutdatedRollingStartIntervalNumberDoNotGetSaved() {
    Collection<TemporaryExposureKey> keys = buildPayloadWithMultipleKeys();
    TemporaryExposureKey outdatedKey = createOutdatedKey();
    keys.add(outdatedKey);
    ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    executor.executeRequest(keys, buildOkHeaders());

    verify(diagnosisKeyService, atLeastOnce()).saveDiagnosisKeys(argument.capture());
    keys.remove(outdatedKey);
    assertElementsCorrespondToEachOther(keys, argument.getValue());
  }

  @Test
  void checkSaveOperationCallForValidParameters() {
    Collection<TemporaryExposureKey> keys = buildPayloadWithMultipleKeys();
    ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    executor.executeRequest(keys, buildOkHeaders());

    verify(diagnosisKeyService, atLeastOnce()).saveDiagnosisKeys(argument.capture());
    assertElementsCorrespondToEachOther(keys, argument.getValue());
  }

  @ParameterizedTest
  @MethodSource("createIncompleteHeaders")
  void badRequestIfCwaHeadersMissing(HttpHeaders headers) {
    ResponseEntity<Void> actResponse = executor.executeRequest(buildPayloadWithOneKey(), headers);

    verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  private static Stream<Arguments> createIncompleteHeaders() {
    return Stream.of(
        Arguments.of(setContentTypeProtoBufHeader(new HttpHeaders())),
        Arguments.of(setContentTypeProtoBufHeader(setCwaFakeHeader(new HttpHeaders(), "0"))),
        Arguments.of(setContentTypeProtoBufHeader(setCwaAuthHeader(new HttpHeaders()))));
  }

  @ParameterizedTest
  @MethodSource("createDeniedHttpMethods")
  void checkOnlyPostAllowed(HttpMethod deniedHttpMethod) {
    // INTERNAL_SERVER_ERROR is the result of blocking by StrictFirewall for non POST calls.
    //                       We can change this when Spring Security 5.4.x is released.
    // METHOD_NOT_ALLOWED is the result of TRACE calls (disabled by default in tomcat)
    List<HttpStatus> allowedErrors = Arrays.asList(INTERNAL_SERVER_ERROR, FORBIDDEN, METHOD_NOT_ALLOWED);

    HttpStatus actStatus = testRestTemplate
        .exchange(SUBMISSION_URL, deniedHttpMethod, null, Void.class).getStatusCode();

    assertThat(allowedErrors)
        .withFailMessage(deniedHttpMethod + " resulted in unexpected status: " + actStatus)
        .contains(actStatus);
  }

  private static Stream<Arguments> createDeniedHttpMethods() {
    return Arrays.stream(HttpMethod.values())
        .filter(method -> method != HttpMethod.POST)
        .filter(method -> method != HttpMethod.PATCH) /* not supported by Rest Template */
        .map(elem -> Arguments.of(elem));
  }

  @Test
  void invalidTanHandling() {
    when(tanVerifier.verifyTan(anyString())).thenReturn(false);

    ResponseEntity<Void> actResponse = executor.executeRequest(buildPayloadWithOneKey(), buildOkHeaders());

    verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    assertThat(actResponse.getStatusCode()).isEqualTo(FORBIDDEN);
  }

  @Test
  void fakeRequestHandling() {
    HttpHeaders headers = buildOkHeaders();
    setCwaFakeHeader(headers, "1");

    ResponseEntity<Void> actResponse = executor.executeRequest(buildPayloadWithOneKey(), headers);

    verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  private static Collection<TemporaryExposureKey> buildPayloadWithOneKey() {
    return Collections.singleton(buildTemporaryExposureKey(VALID_KEY_DATA_1, 1, 3));
  }

  private Collection<TemporaryExposureKey> buildPayloadWithMultipleKeys() {
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(config.getRetentionDays() - 1);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + DiagnosisKey.EXPECTED_ROLLING_PERIOD;
    int rollingStartIntervalNumber3 = rollingStartIntervalNumber2 + DiagnosisKey.EXPECTED_ROLLING_PERIOD;
    return Stream.of(
        buildTemporaryExposureKey(VALID_KEY_DATA_1, rollingStartIntervalNumber1, 3),
        buildTemporaryExposureKey(VALID_KEY_DATA_2, rollingStartIntervalNumber3, 6),
        buildTemporaryExposureKey(VALID_KEY_DATA_3, rollingStartIntervalNumber2, 8))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private Collection<TemporaryExposureKey> buildPayloadWithSingleOutdatedKey() {
    TemporaryExposureKey outdatedKey = createOutdatedKey();
    return Stream.of(outdatedKey).collect(Collectors.toCollection(ArrayList::new));
  }

  private TemporaryExposureKey createOutdatedKey() {
    return TemporaryExposureKey.newBuilder()
        .setKeyData(ByteString.copyFromUtf8(VALID_KEY_DATA_2))
        .setRollingStartIntervalNumber(createRollingStartIntervalNumber(config.getRetentionDays()))
        .setRollingPeriod(DiagnosisKey.EXPECTED_ROLLING_PERIOD)
        .setTransmissionRiskLevel(5).build();
  }
  private static Collection<TemporaryExposureKey> buildPayloadWithInvalidKey() {
    return Stream.of(
        buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(2), 999))
        .collect(Collectors.toCollection(ArrayList::new));
  }
  private void assertElementsCorrespondToEachOther
      (Collection<TemporaryExposureKey> submittedKeys, Collection<DiagnosisKey> keyEntities) {
    Set<DiagnosisKey> expKeys = submittedKeys.stream()
        .map(aSubmittedKey -> DiagnosisKey.builder().fromProtoBuf(aSubmittedKey).build())
        .collect(Collectors.toSet());

    assertThat(keyEntities.size())
        .withFailMessage("Number of submitted keys and generated key entities don't match.")
        .isEqualTo(expKeys.size());
    keyEntities.forEach(anActKey -> assertThat(expKeys)
        .withFailMessage("Key entity does not correspond to a submitted key.")
        .contains(anActKey)
    );
  }
}
