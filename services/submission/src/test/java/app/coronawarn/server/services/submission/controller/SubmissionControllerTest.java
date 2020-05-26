/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.services.submission.controller;

import static java.time.ZoneOffset.UTC;
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
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import com.google.protobuf.ByteString;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
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
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
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

  @BeforeEach
  public void setUpMocks() {
    when(tanVerifier.verifyTan(anyString())).thenReturn(true);
  }

  @Test
  void checkResponseStatusForValidParameters() {
    ResponseEntity<Void> actResponse =
        executeRequest(buildPayloadWithMultipleKeys(), buildOkHeaders());

    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @Test
  void check400ResponseStatusForInvalidParameters() {
    ResponseEntity<Void> actResponse =
        executeRequest(buildPayloadWithInvalidKey(), buildOkHeaders());

    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void check400ResponseStatusForMissingKeys() {
    ResponseEntity<Void> actResponse =
        executeRequest(new ArrayList<>(), buildOkHeaders());

    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void check400ResponseStatusForTooManyKeys() {
    ResponseEntity<Void> actResponse =
        executeRequest(buildPayloadWithTooManyKeys(), buildOkHeaders());

    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void singleKeyWithOutdatedRollingStartIntervalNumberDoesNotGetSaved() {
    Collection<TemporaryExposureKey> keys = buildPayloadWithSingleOutdatedKey();
    ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    executeRequest(keys, buildOkHeaders());

    verify(diagnosisKeyService, atLeastOnce()).saveDiagnosisKeys(argument.capture());
    assertThat(argument.getValue()).isEmpty();
  }

  @Test
  void keysWithOutdatedRollingStartIntervalNumberDoNotGetSaved() {
    Collection<TemporaryExposureKey> keys = buildPayloadWithMultipleKeys();
    TemporaryExposureKey outdatedKey = createOutdatedKey();
    keys.add(outdatedKey);
    ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    executeRequest(keys, buildOkHeaders());

    verify(diagnosisKeyService, atLeastOnce()).saveDiagnosisKeys(argument.capture());
    keys.remove(outdatedKey);
    assertElementsCorrespondToEachOther(keys, argument.getValue());
  }

  @Test
  void checkSaveOperationCallForValidParameters() {
    Collection<TemporaryExposureKey> keys = buildPayloadWithMultipleKeys();
    ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    executeRequest(keys, buildOkHeaders());

    verify(diagnosisKeyService, atLeastOnce()).saveDiagnosisKeys(argument.capture());
    assertElementsCorrespondToEachOther(keys, argument.getValue());
  }

  @ParameterizedTest
  @MethodSource("createIncompleteHeaders")
  void badRequestIfCwaHeadersMissing(HttpHeaders headers) {
    ResponseEntity<Void> actResponse = executeRequest(buildPayloadWithOneKey(), headers);

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

    ResponseEntity<Void> actResponse =
        executeRequest(buildPayloadWithOneKey(), buildOkHeaders());

    verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    assertThat(actResponse.getStatusCode()).isEqualTo(FORBIDDEN);
  }

  @Test
  void fakeRequestHandling() {
    HttpHeaders headers = buildOkHeaders();
    setCwaFakeHeader(headers, "1");

    ResponseEntity<Void> actResponse = executeRequest(buildPayloadWithOneKey(), headers);

    verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  private static HttpHeaders buildOkHeaders() {
    HttpHeaders headers = setCwaAuthHeader(setContentTypeProtoBufHeader(new HttpHeaders()));

    return setCwaFakeHeader(headers, "0");
  }

  private static HttpHeaders setContentTypeProtoBufHeader(HttpHeaders headers) {
    headers.setContentType(MediaType.valueOf("application/x-protobuf"));
    return headers;
  }

  private static HttpHeaders setCwaAuthHeader(HttpHeaders headers) {
    headers.set("cwa-authorization", "TAN okTan");
    return headers;
  }

  private static HttpHeaders setCwaFakeHeader(HttpHeaders headers, String value) {
    headers.set("cwa-fake", value);
    return headers;
  }

  private static Collection<TemporaryExposureKey> buildPayloadWithOneKey() {
    return Collections.singleton(buildTemporaryExposureKey("testKey111111111", 1, 2, 3));
  }

  private static Collection<TemporaryExposureKey> buildPayloadWithMultipleKeys() {
    return Stream.of(
        buildTemporaryExposureKey("testKey111111111", createRollingStartIntervalNumber(2), 2, 3),
        buildTemporaryExposureKey("testKey222222222", createRollingStartIntervalNumber(4), 5, 6),
        buildTemporaryExposureKey("testKey333333333", createRollingStartIntervalNumber(10), 8, 8))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private static Collection<TemporaryExposureKey> buildPayloadWithSingleOutdatedKey() {
    TemporaryExposureKey outdatedKey = createOutdatedKey();
    return Stream.of(outdatedKey).collect(Collectors.toCollection(ArrayList::new));
  }

  private static TemporaryExposureKey createOutdatedKey() {
    return TemporaryExposureKey.newBuilder()
        .setKeyData(ByteString.copyFromUtf8("testKey222222222"))
        .setRollingStartIntervalNumber(createRollingStartIntervalNumber(99))
        .setRollingPeriod(10)
        .setTransmissionRiskLevel(5).build();
  }

  private Collection<TemporaryExposureKey> buildPayloadWithTooManyKeys() {
    ArrayList<TemporaryExposureKey> tooMany = new ArrayList<>();
    for (int i = 0; i <= 99; i++) {
      tooMany.add(
          buildTemporaryExposureKey("testKey111111111", createRollingStartIntervalNumber(2), 2, 3));
    }

    return tooMany;
  }

  private static Collection<TemporaryExposureKey> buildPayloadWithInvalidKey() {
    return Stream.of(
        buildTemporaryExposureKey("testKey111111111", createRollingStartIntervalNumber(2), 2, 999))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private static int createRollingStartIntervalNumber(Integer daysAgo) {
    return Math.toIntExact(LocalDate
        .ofInstant(Instant.now(), UTC)
        .minusDays(daysAgo).atStartOfDay()
        .toEpochSecond(UTC) / (60 * 10));
  }

  private static TemporaryExposureKey buildTemporaryExposureKey(
      String keyData, int rollingStartIntervalNumber, int rollingPeriod, int transmissionRiskLevel) {
    return TemporaryExposureKey.newBuilder()
        .setKeyData(ByteString.copyFromUtf8(keyData))
        .setRollingStartIntervalNumber(rollingStartIntervalNumber)
        .setRollingPeriod(rollingPeriod)
        .setTransmissionRiskLevel(transmissionRiskLevel).build();
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

  private ResponseEntity<Void> executeRequest(Collection<TemporaryExposureKey> keys, HttpHeaders headers) {
    SubmissionPayload body = SubmissionPayload.newBuilder().addAllKeys(keys).build();
    RequestEntity<SubmissionPayload> request =
        new RequestEntity<>(body, headers, HttpMethod.POST, SUBMISSION_URL);
    return testRestTemplate.postForEntity(SUBMISSION_URL, request, Void.class);
  }
}
