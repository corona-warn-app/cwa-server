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

import app.coronawarn.server.common.persistence.domain.FederationBatch;
import app.coronawarn.server.services.federationdownload.config.FederationDownloadServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


/**
 * The BatchDownloader downloads the batches containing the keys.
 */
@Service
public class DiagnosisKeyBatchDownloader {

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyBatchDownloader.class);

  private final FederationDownloadServiceConfig federationDownloadServiceConfig;
  private final WebClient webClient;

  public DiagnosisKeyBatchDownloader(FederationDownloadServiceConfig federationDownloadServiceConfig,
                                     WebClient.Builder webClientBuilder) {
    this.federationDownloadServiceConfig = federationDownloadServiceConfig;
    this.webClient = webClientBuilder.baseUrl(federationDownloadServiceConfig.getFederationDownloadBaseUrl()).build();
  }

  /**
   * Used to download the batches.
   *
   * @param federationBatch Contains the BatchTag and date
   * @return Returns the downloaded batch
   * @throws RestClientException if status code is neither 2xx nor 4xx
   */
  public Mono<byte[]> downloadBatch(FederationBatch federationBatch)
      throws Exception {
    try {
      logger.info("Calling federation gateway download service for batch download ...");

      Mono<byte[]> mono = webClient.get()
          .uri(uriBuilder -> uriBuilder
              .path(federationDownloadServiceConfig.getFederationDownloadPath())
              .path(federationBatch.getDate().toString())
              .build())
          .exchange()
          .flatMap(response -> {
            if (response.statusCode().is2xxSuccessful()) {
              return response.bodyToMono(ByteArrayResource.class);
            } else {
              return response.bodyToMono(Void.class).then(Mono.empty());
            }
          })
          .map(ByteArrayResource::getByteArray);

      logger.info("Received batch from federation gateway service");
      return mono;
    } catch (Exception e) {
      logger.info("Federation gateway service error");
      throw new Exception(e.getMessage());
    }
  }
}
