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

package app.coronawarn.server.services.federation.download.download;

import app.coronawarn.server.services.federation.download.config.FederationDownloadServiceConfig;
import java.time.LocalDate;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


/**
 * The BatchDownloader downloads the batches containing the keys.
 */
@Service
public class DiagnosisKeyBatchDownloader implements DiagnosisKeyBatchDownloaders {

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyBatchDownloader.class);

  private final FederationDownloadServiceConfig federationDownloadServiceConfig;
  private final WebClient webClient;

  public DiagnosisKeyBatchDownloader(FederationDownloadServiceConfig federationDownloadServiceConfig,
                                     WebClient.Builder webClientBuilder) {
    this.federationDownloadServiceConfig = federationDownloadServiceConfig;
    this.webClient = webClientBuilder.baseUrl(federationDownloadServiceConfig.getFederationDownloadBaseUrl()).build();
  }

  @Override
  public DiagnosisKeyBatchContainer downloadBatch(LocalDate date) {
    return null;
    /*
    try {
      logger.info("Calling federation gateway download service for batch download");
      Mono<byte[]> mono = getMono(date);
      logger.info("Received batch from federation gateway service");
      return new DiagnosisKeyBatchContainer(DiagnosisKeyBatch.parseFrom(mono.block()), null, null);
    } catch (Exception e) {
      logger.info("Federation gateway service error");
      return null;
      // throw new Exception(e.getMessage());
    }
     */
  }

  @Override
  public DiagnosisKeyBatchContainer downloadBatch(LocalDate date, String batchTag) {
    return null;
  }

  /*
  private Mono<byte[]> getMono(Date date) {
    Mono<byte[]> mono = webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path(federationDownloadServiceConfig.getFederationDownloadPath())
            .path(date.toString()) // TODO simpledateformat?
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
    return mono;
  }
   */
}
