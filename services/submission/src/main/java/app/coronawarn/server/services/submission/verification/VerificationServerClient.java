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

package app.coronawarn.server.services.submission.verification;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * This is a Spring Cloud Feign based HTTP client that allows type-safe HTTP calls
 * and abstract the implementation away.
 */
@FeignClient(name = "verification-server", url = "${services.submission.verification.base-url}")
public interface VerificationServerClient {

  /**
   * This methods calls the verification service with the given
   * {#link tan}.
   * @param tan the tan to verify.
   * @return 404 when the tan is not valid.
   */
  @PostMapping(value = "${services.submission.verification.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
  String verifyTan(Tan tan);
}
