package app.coronawarn.server.services.submission.controller;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.protocols.external.exposurenotification.Key;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import com.google.protobuf.ByteString;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
public class SubmissionControllerTest {

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
  public void checkResponseStatusForValidParameters() {
    ResponseEntity<Void> actResponse =
        executeRequest(buildPayloadWithMultipleKeys(), buildOkHeaders());

    assertEquals(HttpStatus.OK, actResponse.getStatusCode());
  }

  @Test
  public void checkSaveOperationCallForValidParameters() {
    Collection<Key> keys = buildPayloadWithMultipleKeys();
    ArgumentCaptor<Collection<DiagnosisKey>> argument = ArgumentCaptor.forClass(Collection.class);

    executeRequest(keys, buildOkHeaders());

    verify(diagnosisKeyService, atLeastOnce()).saveDiagnosisKeys(argument.capture());
    assertElementsCorrespondToEachOther(keys, argument.getValue());
  }

  @ParameterizedTest
  @MethodSource("createIncompleteHeaders")
  public void badRequestIfCwaHeadersMissing(HttpHeaders headers) {
    ResponseEntity<Void> actResponse = executeRequest(buildPayloadWithOneKey(), headers);

    verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    assertEquals(HttpStatus.BAD_REQUEST, actResponse.getStatusCode());
  }

  private static Stream<Arguments> createIncompleteHeaders() {
    return Stream.of(
        Arguments.of(setContentTypeProtoBufHeader(new HttpHeaders())),
        Arguments.of(setContentTypeProtoBufHeader(setCwaFakeHeader(new HttpHeaders(), "0"))),
        Arguments.of(setContentTypeProtoBufHeader(setCwaAuthHeader(new HttpHeaders()))));
  }

  @Test
  public void checkAcceptedHttpMethods() {
    Set<HttpMethod> expAllowedMethods =
        Stream.of(HttpMethod.POST, HttpMethod.OPTIONS)
            .collect(Collectors.toCollection(HashSet::new));

    Set<HttpMethod> actAllowedMethods = testRestTemplate.optionsForAllow(SUBMISSION_URL.toString());

    assertEquals(expAllowedMethods, actAllowedMethods);
  }

  @Test
  public void invalidTanHandling() {
    when(tanVerifier.verifyTan(anyString())).thenReturn(false);

    ResponseEntity<Void> actResponse =
        executeRequest(buildPayloadWithOneKey(), buildOkHeaders());

    verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    assertEquals(HttpStatus.FORBIDDEN, actResponse.getStatusCode());
  }

  @Test
  public void fakeRequestHandling() {
    HttpHeaders headers = buildOkHeaders();
    setCwaFakeHeader(headers, "1");

    ResponseEntity<Void> actResponse = executeRequest(buildPayloadWithOneKey(), headers);

    verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    assertEquals(HttpStatus.OK, actResponse.getStatusCode());
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
    headers.set("cwa-authorization", "okTan");
    return headers;
  }

  private static HttpHeaders setCwaFakeHeader(HttpHeaders headers, String value) {
    headers.set("cwa-fake", value);
    return headers;
  }

  private static Collection<Key> buildPayloadWithOneKey() {
    return Collections.singleton(buildTemporaryExposureKey("testKey111111111", 1, 2, 3));
  }

  private static Collection<Key> buildPayloadWithMultipleKeys() {
    return Stream.of(
        buildTemporaryExposureKey("testKey111111111", 1, 2, 3),
        buildTemporaryExposureKey("testKey222222222", 4, 5, 6),
        buildTemporaryExposureKey("testKey333333333", 7, 8, 9))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private static Key buildTemporaryExposureKey(
      String keyData, int rollingStartNumber, int rollingPeriod, int transmissionRiskLevel) {
    return Key.newBuilder()
        .setKeyData(ByteString.copyFromUtf8(keyData))
        .setRollingStartNumber(rollingStartNumber)
        .setRollingPeriod(rollingPeriod)
        .setTransmissionRiskLevel(transmissionRiskLevel).build();
  }

  private void assertElementsCorrespondToEachOther
      (Collection<Key> submittedKeys, Collection<DiagnosisKey> keyEntities) {
    Set<DiagnosisKey> expKeys = submittedKeys.stream()
        .map(aSubmittedKey -> DiagnosisKey.builder().fromProtoBuf(aSubmittedKey).build())
        .collect(Collectors.toSet());

    assertEquals(expKeys.size(), keyEntities.size(),
        "Number of submitted keys and generated key entities don't match.");
    keyEntities.stream().forEach(anActKey -> assertTrue(expKeys.contains(anActKey),
        "Key entity does not correspond to a submitted key."));
  }

  private ResponseEntity<Void> executeRequest(Collection<Key> keys, HttpHeaders headers) {
    SubmissionPayload body = SubmissionPayload.newBuilder().addAllKeys(keys).build();
    RequestEntity<SubmissionPayload> request =
        new RequestEntity<>(body, headers, HttpMethod.POST, SUBMISSION_URL);
    return testRestTemplate.postForEntity(SUBMISSION_URL, request, Void.class);
  }
}
