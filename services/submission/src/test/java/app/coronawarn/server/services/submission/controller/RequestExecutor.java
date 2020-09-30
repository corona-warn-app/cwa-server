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

  private final TestRestTemplate testRestTemplate;

  public RequestExecutor(TestRestTemplate testRestTemplate) {
    this.testRestTemplate = testRestTemplate;
  }

  public ResponseEntity<Void> execute(HttpMethod method, RequestEntity<SubmissionPayload> requestEntity) {
    return testRestTemplate.exchange(SUBMISSION_URL, method, requestEntity, Void.class);
  }

  public ResponseEntity<Void> executePost(Collection<TemporaryExposureKey> keys, HttpHeaders headers) {
    SubmissionPayload body = SubmissionPayload.newBuilder()
        .setOrigin("DE")
        .addAllVisitedCountries(List.of("DE"))
        .addAllKeys(keys).build();
    return executePost(body, headers);
  }

  public ResponseEntity<Void> executePost(SubmissionPayload body, HttpHeaders headers) {
    return execute(HttpMethod.POST, new RequestEntity<>(body, headers, HttpMethod.POST, SUBMISSION_URL));
  }

  public ResponseEntity<Void> executePost(SubmissionPayload body) {
    return executePost(body, buildDefaultHeader());
  }

  public ResponseEntity<Void> executePost(Collection<TemporaryExposureKey> keys) {
    return executePost(keys, buildDefaultHeader());
  }

  private HttpHeaders buildDefaultHeader() {
    return HttpHeaderBuilder.builder()
        .contentTypeProtoBuf()
        .cwaAuth()
        .withoutCwaFake()
        .build();
  }

  public static TemporaryExposureKey buildTemporaryExposureKey(
      String keyData, int rollingStartIntervalNumber, int transmissionRiskLevel) {
    return TemporaryExposureKey.newBuilder()
        .setKeyData(ByteString.copyFromUtf8(keyData))
        .setRollingStartIntervalNumber(rollingStartIntervalNumber)
        .setTransmissionRiskLevel(transmissionRiskLevel).build();
  }

  public static TemporaryExposureKey buildTemporaryExposureKeyWithFlexibleRollingPeriod(
      String keyData, int rollingStartIntervalNumber, int transmissionRiskLevel, int rollingPeriod) {
    return TemporaryExposureKey.newBuilder()
        .setKeyData(ByteString.copyFromUtf8(keyData))
        .setRollingStartIntervalNumber(rollingStartIntervalNumber)
        .setTransmissionRiskLevel(transmissionRiskLevel)
        .setRollingPeriod(rollingPeriod).build();
  }

  public static int createRollingStartIntervalNumber(Integer daysAgo) {
    return Math.toIntExact(LocalDate
        .ofInstant(Instant.now(), UTC)
        .minusDays(daysAgo).atStartOfDay()
        .toEpochSecond(UTC) / (60 * 10));
  }

  public static Collection<TemporaryExposureKey> buildPayloadWithOneKey() {
    return Collections.singleton(buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(1), 3));
  }
}
