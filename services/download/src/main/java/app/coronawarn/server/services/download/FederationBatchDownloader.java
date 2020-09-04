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

package app.coronawarn.server.services.download;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.federation.client.download.BatchDownloadResponse;
import java.time.LocalDate;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Manages querying the federation gateway service and parsing of the responses.
 */
@Service
public class FederationBatchDownloader {

  private static final Logger logger = LoggerFactory.getLogger(FederationBatchDownloader.class);
  private final FederationGatewayClient federationGatewayClient;

  /**
   * Creates a FederationBatchDownloader.
   */
  public FederationBatchDownloader(FederationGatewayClient federationGatewayClient) {
    this.federationGatewayClient = federationGatewayClient;
  }

  /**
   * Downloads the first batch of the specified date.
   *
   * @param date the date for which the first batch should be downloaded
   * @return The server response.
   */
  public Optional<BatchDownloadResponse> downloadFirstBatch(LocalDate date) {
    try {
      logger.info("Downloading first batch for date {} started", date);
      return Optional.of(federationGatewayClient.getDiagnosisKeys(date.format(ISO_LOCAL_DATE)));
    } catch (Exception e) {
      logger.error("Downloading batch for date {} failed", date, e);
      return Optional.empty();
    }
  }

  /**
   * Downloads the batch specified for this date.
   *
   * @param date the date for which the batch should be downloaded
   * @return The server response.
   */
  public Optional<BatchDownloadResponse> downloadBatch(LocalDate date, String batchTag) {
    try {
      logger.info("Downloading batch for date {} and batchTag {} started", date, batchTag);
      return Optional.of(federationGatewayClient.getDiagnosisKeys(batchTag, date.format(ISO_LOCAL_DATE)));
    } catch (Exception e) {
      logger.error("Downloading batch for date {} and batchTag {} failed", date, batchTag, e);
      return Optional.empty();
    }
  }
}
