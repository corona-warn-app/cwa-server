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

import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import com.google.protobuf.ByteString;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * RequestExecutor executes requests against the diagnosis key submission endpoint and holds a various methods for test
 * request generation.
 */
@Component
public class RequestExecutor {
  public static final String VALID_KEY_DATA_1 = "testKey111111111";
  public static final String VALID_KEY_DATA_2 = "testKey222222222";
  public static final String VALID_KEY_DATA_3 = "testKey333333333";
  private static final URI SUBMISSION_URL = URI.create("/version/v1/diagnosis-keys");
  private final TestRestTemplate testRestTemplate;

  public RequestExecutor(TestRestTemplate testRestTemplate) {
    this.testRestTemplate = testRestTemplate;
  }

  public ResponseEntity<Void> executeRequest(Collection<TemporaryExposureKey> keys, HttpHeaders headers) {
    SubmissionPayload body = SubmissionPayload.newBuilder().addAllKeys(keys).build();
    RequestEntity<SubmissionPayload> request =
        new RequestEntity<>(body, headers, HttpMethod.POST, SUBMISSION_URL);
    return testRestTemplate.postForEntity(SUBMISSION_URL, request, Void.class);
  }

  public static HttpHeaders buildOkHeaders() {
    HttpHeaders headers = setCwaAuthHeader(setContentTypeProtoBufHeader(new HttpHeaders()));

    return setCwaFakeHeader(headers, "0");
  }

  public static HttpHeaders setContentTypeProtoBufHeader(HttpHeaders headers) {
    headers.setContentType(MediaType.valueOf("application/x-protobuf"));
    return headers;
  }

  public static HttpHeaders setCwaAuthHeader(HttpHeaders headers) {
    headers.set("cwa-authorization", "TAN okTan");
    return headers;
  }

  public static HttpHeaders setCwaFakeHeader(HttpHeaders headers, String value) {
    headers.set("cwa-fake", value);
    return headers;
  }

  public static TemporaryExposureKey buildTemporaryExposureKey(
      String keyData, int rollingStartIntervalNumber, int transmissionRiskLevel) {
    return TemporaryExposureKey.newBuilder()
        .setKeyData(ByteString.copyFromUtf8(keyData))
        .setRollingStartIntervalNumber(rollingStartIntervalNumber)
        .setTransmissionRiskLevel(transmissionRiskLevel).build();
  }

  public static int createRollingStartIntervalNumber(Integer daysAgo) {
    return Math.toIntExact(LocalDate
        .ofInstant(Instant.now(), UTC)
        .minusDays(daysAgo).atStartOfDay()
        .toEpochSecond(UTC) / (60 * 10));
  }
}
