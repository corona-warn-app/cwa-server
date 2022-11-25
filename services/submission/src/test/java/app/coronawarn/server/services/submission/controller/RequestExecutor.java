package app.coronawarn.server.services.submission.controller;

import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * RequestExecutor executes requests against the diagnosis key submission endpoint and holds a various methods for test
 * request generation.
 */
@Component
public class RequestExecutor {

  private static final URI SUBMISSION_URL = URI.create("/version/v1/diagnosis-keys");

  private static final URI SUBMISSION_ON_BEHALF_URL = URI.create("/version/v1/submission-on-behalf");

  private final TestRestTemplate testRestTemplate;

  public RequestExecutor(final TestRestTemplate testRestTemplate) {
    this.testRestTemplate = testRestTemplate;
  }

  private HttpHeaders buildDefaultHeader() {
    return HttpHeaderBuilder.builder()
        .contentTypeProtoBuf()
        .cwaAuth()
        .withoutCwaFake()
        .build();
  }

  private HttpHeaders buildSrsHeader() {
    return HttpHeaderBuilder.builder()
        .contentTypeProtoBuf()
        .cwaOtp()
        .withoutCwaFake()
        .build();
  }

  public ResponseEntity<Void> execute(final HttpMethod method, final RequestEntity<SubmissionPayload> requestEntity) {
    return testRestTemplate.exchange(SUBMISSION_URL, method, requestEntity, Void.class);
  }

  public ResponseEntity<Void> executePost(final Collection<TemporaryExposureKey> keys) {
    return executePost(keys, buildDefaultHeader());
  }

  public ResponseEntity<Void> executePost(final Collection<TemporaryExposureKey> keys, final HttpHeaders headers) {
    final SubmissionPayload body = SubmissionPayload.newBuilder()
        .setOrigin("DE")
        .addAllVisitedCountries(List.of("DE"))
        .addAllKeys(keys).build();
    return executePost(body, headers);
  }

  public ResponseEntity<Void> executePost(final SubmissionPayload body) {
    return executePost(body, buildDefaultHeader());
  }

  public ResponseEntity<Void> executeSrsPost(final SubmissionPayload body) {
    return executePost(body, buildSrsHeader());
  }

  public ResponseEntity<Void> executePost(final SubmissionPayload body, final HttpHeaders headers) {
    return execute(HttpMethod.POST, new RequestEntity<>(body, headers, HttpMethod.POST, SUBMISSION_URL));
  }

  public ResponseEntity<Void> executeSubmissionOnBehalf(final HttpMethod method,
      final RequestEntity<SubmissionPayload> requestEntity) {
    return testRestTemplate.exchange(SUBMISSION_ON_BEHALF_URL, method, requestEntity, Void.class);
  }

  public ResponseEntity<Void> executeSubmissionOnBehalf(final SubmissionPayload body) {
    return executeSubmissionOnBehalf(body, buildDefaultHeader());
  }

  public ResponseEntity<Void> executeSubmissionOnBehalf(final SubmissionPayload body, final HttpHeaders headers) {
    return executeSubmissionOnBehalf(HttpMethod.POST,
        new RequestEntity<>(body, headers, HttpMethod.POST, SUBMISSION_ON_BEHALF_URL));
  }
}
