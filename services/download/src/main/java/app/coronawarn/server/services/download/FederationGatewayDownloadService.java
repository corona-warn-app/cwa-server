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
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import feign.FeignException;
import java.time.LocalDate;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Responsible for downloading and storing batch information from the federation gateway.
 */
@Component
public class FederationGatewayDownloadService {

  public static final String HEADER_BATCH_TAG = "batchTag";
  public static final String HEADER_NEXT_BATCH_TAG = "nextBatchTag";
  public static final String EMPTY_HEADER = "null";
  private static final Logger logger = LoggerFactory.getLogger(FederationGatewayDownloadService.class);
  private final FederationGatewayClient federationGatewayClient;

  /**
   * Constructor.
   *
   * @param federationGatewayClient A {@link FederationGatewayClient} for retrieving federation diagnosis key batches.
   */
  public FederationGatewayDownloadService(FederationGatewayClient federationGatewayClient) {
    this.federationGatewayClient = federationGatewayClient;
  }

  /**
   * Download the first batch from the EFGS for the given date.
   *
   * @param date The date for which the batch should be downloaded.
   * @return The {@link BatchDownloadResponse} containing the downloaded batch, batchTag and nextBatchTag.
   */
  public BatchDownloadResponse downloadBatch(LocalDate date) {
    try {
      logger.info("Downloading first batch for date {}", date);
      ResponseEntity<DiagnosisKeyBatch> response = federationGatewayClient
          .getDiagnosisKeys(date.format(ISO_LOCAL_DATE));
      return parseResponseEntity(response);
    } catch (FeignException e) {
      throw new FederationGatewayException("Downloading batch for date " + date.format(ISO_LOCAL_DATE) + " failed.", e);
    }
  }

  /**
   * Download the batch from the EFGS with the given batchTag for the given date.
   *
   * @param batchTag The batchTag of the batch that should be downloaded.
   * @param date     The date for which the batch should be downloaded.
   * @return The {@link BatchDownloadResponse} containing the downloaded batch, batchTag and nextBatchTag.
   */
  public BatchDownloadResponse downloadBatch(String batchTag, LocalDate date) {
    String dateString = date.format(ISO_LOCAL_DATE);
    try {
      logger.info("Downloading batch for date {} and batchTag {}.", batchTag, dateString);
      ResponseEntity<DiagnosisKeyBatch> response = federationGatewayClient
          .getDiagnosisKeys(batchTag, dateString);
      return parseResponseEntity(response);
    } catch (FeignException e) {
      logger.error("Downloading batch for date {} and batchTag {} failed.", batchTag, dateString);
      throw new FederationGatewayException("Downloading batch " + batchTag + " for date " + date + " failed.", e);
    }
  }

  private BatchDownloadResponse parseResponseEntity(ResponseEntity<DiagnosisKeyBatch> response) {
    String batchTag = getHeader(response, HEADER_BATCH_TAG)
        .orElseThrow(() -> new FederationGatewayException("Missing " + HEADER_BATCH_TAG + " header."));
    Optional<String> nextBatchTag = getHeader(response, HEADER_NEXT_BATCH_TAG);
    return new BatchDownloadResponse(batchTag, Optional.ofNullable(response.getBody()), nextBatchTag);
  }

  private Optional<String> getHeader(ResponseEntity<DiagnosisKeyBatch> response, String header) {
    String headerString = response.getHeaders().getFirst(header);
    return (!EMPTY_HEADER.equals(headerString))
        ? Optional.ofNullable(headerString)
        : Optional.empty();
  }
}
