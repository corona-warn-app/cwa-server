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

package app.coronawarn.server.services.callback.controller;

import java.net.URI;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * RequestExecutor executes requests against the diagnosis key callback endpoint for testing purposes.
 */
@Component
public class RequestExecutor {

  private static final URI CALLBACK_URL = URI.create("/version/v1/callback");

  private final TestRestTemplate testRestTemplate;

  public RequestExecutor(TestRestTemplate testRestTemplate) {
    this.testRestTemplate = testRestTemplate;
  }

  public ResponseEntity<Void> executeGet(String batchTag, String dateString) {
    return testRestTemplate.getForEntity(buildRequestUrl(batchTag, dateString), Void.class);
  }

  private String buildRequestUrl(String batchTag, String dateString) {
    StringBuilder requestUrl = new StringBuilder();
    requestUrl.append(CALLBACK_URL);

    if (batchTag != null || dateString != null) {
      requestUrl.append("?");
    }
    if (batchTag != null) {
      requestUrl.append("batchTag=" + batchTag);
    }
    if (batchTag != null && dateString != null) {
      requestUrl.append("&");
    }
    if (dateString != null) {
      requestUrl.append("date=" + dateString);
    }

    return requestUrl.toString();
  }
}
