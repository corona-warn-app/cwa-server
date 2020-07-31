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

package app.coronawarn.server.services.federationdownload.download;

import app.coronawarn.server.common.persistence.domain.FederationBatchDownload;
import feign.Headers;
import feign.Response;
import io.micrometer.core.annotation.Timed;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * This is a Spring Cloud Feign based HTTP client that allows type-safe HTTP calls and abstract the implementation
 * away.
 */
@Validated
@FeignClient(name = "download-server", configuration = DownloadServerClientConfiguration.class,
    url = "${services.federationdownload.federationgateway.base-url}")
public interface DownloadServerClient {

  /**
   * This methods calls the download service with the given batchTag & date.
   */
  @Timed
  @Headers("Content-Type: application/protobuf; version=1.0")
  @GetMapping(value = "${services.federationdownload.federationgateway.path}")
  Response downloadBatch(@SpringQueryMap FederationBatchDownload params);

}
