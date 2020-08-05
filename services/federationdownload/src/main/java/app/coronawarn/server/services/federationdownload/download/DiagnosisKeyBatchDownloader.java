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
import feign.Response;
import feign.Response.Body;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;


/**
 * The BatchDownloader downloads the batches containing the keys.
 */
@Service
public class DiagnosisKeyBatchDownloader {

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyBatchDownloader.class);
  private final FederationGatewayClient federationGatewayClient;

  /**
   * This class can be used to get the batches of the configured federation gateway.
   *
   * @param federationGatewayClient The REST client to communicate with the federation gateway server
   */
  public DiagnosisKeyBatchDownloader(FederationGatewayClient federationGatewayClient) {
    this.federationGatewayClient = federationGatewayClient;
  }

  /**
   * Used to download the batches.
   *
   * @param federationBatch Contains the BatchTag and date
   * @return Returns the downloaded batch
   * @throws RestClientException if status code is neither 2xx nor 4xx
   */
  public Body downloadBatch(FederationBatch federationBatch)
      throws Exception {
    try {
      logger.info("Calling federation gateway download service for batch download ...");
      Response result = federationGatewayClient.downloadDiagnosisKeyBatch(federationBatch);
      logger.info("Received batch from federation gateway service");
      return result.body();
    } catch (Exception e) {
      logger.info("Federation gateway service error");
      throw new Exception(e.getMessage());
    }
  }
}
