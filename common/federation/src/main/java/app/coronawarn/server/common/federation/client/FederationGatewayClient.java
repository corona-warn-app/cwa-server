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

package app.coronawarn.server.common.federation.client;

import feign.Headers;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Declarative web service client for the Federation Gateway API.
 *
 * <p>Any application that wants to uses it must make sure the required configuration
 * beans in this module are registered (scan root package of the module). There is also
 * a constraint imposed on application properties, such that values for the following
 * structure must be declared:
 * <li> federation-gateway.base-url
 * <li> federation-gateway.ssl.key-store-path
 * <li> federation-gateway.ssl.key-store-pass
 * <li> federation-gateway.ssl.certificate-type
 */
@FeignClient(name = "federation-server", url = "${federation-gateway.base-url}")
public interface FederationGatewayClient {

  @GetMapping(value = "/diagnosiskeys/download/{date}")
  // @Headers({"Accept: application/json; version=1.0", "X-SSL-Client-SHA256: abcd", "X-SSL-Client-DN: C=PL"})
  Response getDiagnosisKeys(@RequestHeader("Accept") String accept,
                            @RequestHeader("X-SSL-Client-SHA256") String shaClient,
                            @RequestHeader("X-SSL-Client-DN") String dnClient,
                            @PathVariable("date") String date);
}