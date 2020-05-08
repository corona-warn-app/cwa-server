package app.coronawarn.server.services.submission.controller;


import static app.coronawarn.server.common.protocols.generated.ExposureKeys.TemporaryExposureKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import com.google.protobuf.ByteString;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
        executeRequest(buildTemporaryExposureKey(), buildOkHeaders());

    assertEquals(HttpStatus.OK, actResponse.getStatusCode());
  }

  @Test
  public void checkSaveOperationCallForValidParameters() {
    executeRequest(buildTemporaryExposureKey(), buildOkHeaders());

    verify(diagnosisKeyService, atLeastOnce()).saveDiagnosisKeys(anyCollection());
  }

  @ParameterizedTest
  @MethodSource("createIncompleteHeaders")
  public void badRequestIfCwaHeadersMissing(HttpHeaders headers) {
    ResponseEntity<Void> actResponse = executeRequest(buildTemporaryExposureKey(), headers);

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
        executeRequest(buildTemporaryExposureKey(), buildOkHeaders());

    verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    assertEquals(HttpStatus.FORBIDDEN, actResponse.getStatusCode());
  }

  @Test
  public void fakeRequestHandling() {
    HttpHeaders headers = buildOkHeaders();
    setCwaFakeHeader(headers, "1");

    ResponseEntity<Void> actResponse = executeRequest(buildTemporaryExposureKey(), headers);

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

  private static TemporaryExposureKey buildTemporaryExposureKey() {
    return buildTemporaryExposureKey("testKey123456789", 3L, 2);
  }

  private static TemporaryExposureKey buildTemporaryExposureKey(
      String keyData, long rollingStartNumber, int riskLevelValue) {
    return TemporaryExposureKey.newBuilder()
        .setKeyData(ByteString.copyFromUtf8(keyData))
        .setRollingStartNumber(rollingStartNumber)
        .setRiskLevelValue(riskLevelValue).build();
  }

  private ResponseEntity<Void> executeRequest(TemporaryExposureKey body, HttpHeaders headers) {
    RequestEntity<TemporaryExposureKey> request =
        new RequestEntity<>(body, headers, HttpMethod.POST, SUBMISSION_URL);
    return testRestTemplate.postForEntity(SUBMISSION_URL, request, Void.class);
  }
}
