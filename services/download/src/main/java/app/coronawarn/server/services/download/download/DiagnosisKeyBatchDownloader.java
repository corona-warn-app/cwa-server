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

package app.coronawarn.server.services.download.download;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import feign.Response;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The BatchDownloader downloads the batches containing the keys.
 */
@Service
public class DiagnosisKeyBatchDownloader {

  public static final String HEADER_BATCH_TAG = "batchTag";
  public static final String HEADER_NEXT_BATCH_TAG = "nextBatchTag";
  public static final String EMPTY_HEADER = "null";

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyBatchDownloader.class);
  private FederationGatewayClient federationGatewayClient;

  /**
   * Creates a DiagnosisKeyBatchDownloader.
   */
  public DiagnosisKeyBatchDownloader(FederationGatewayClient federationGatewayClient) {
    this.federationGatewayClient = federationGatewayClient;
  }

  /**
   * Downloads the batch specified for this date.
   *
   * @param date the date for which the batch should be downloaded
   * @return The server response.
   */
  public Optional<FederationGatewayResponse> downloadBatch(LocalDate date) {
    // TODO try to put headers into client class
    try (Response response = federationGatewayClient.getDiagnosisKeys(
        "application/protobuf; version=1.0",
        "abcd",
        "C=PL",
        date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        InputStream responseBody = response.body().asInputStream()) {
      logger.info("Downloading batch for date " + date + " started");
      String batchTag = getHeader(response, HEADER_BATCH_TAG).orElseThrow();
      Optional<String> nextBatchTag = getHeader(response, HEADER_NEXT_BATCH_TAG);
      DiagnosisKeyBatch diagnosisKeyBatch = DiagnosisKeyBatch.parseFrom(responseBody);
      return Optional.of(new FederationGatewayResponse(diagnosisKeyBatch, batchTag, nextBatchTag, date));
    } catch (Exception e) {
      logger.info("Downloading batch for date " + date + " failed", e);
      return Optional.empty();
    }
  }

  /**
   * Downloads the batch specified for this date.
   *
   * @param date the date for which the batch should be downloaded
   * @return The server response.
   */
  public Optional<FederationGatewayResponse> downloadBatch(LocalDate date, String batchTag) {
    try (Response response = federationGatewayClient.getDiagnosisKeys(
        "application/protobuf; version=1.0",
        "abcd",
        "C=PL",
        batchTag,
        date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        InputStream responseBody = response.body().asInputStream()) {
      logger.info("Downloading batch for date " + date + " and batchTag " + batchTag + " started");
      Optional<String> nextBatchTag = getHeader(response, HEADER_NEXT_BATCH_TAG);
      DiagnosisKeyBatch diagnosisKeyBatch = DiagnosisKeyBatch.parseFrom(responseBody);
      return Optional.of(new FederationGatewayResponse(diagnosisKeyBatch, batchTag, nextBatchTag, date));
    } catch (Exception e) {
      logger.info("Downloading batch for date " + date + " and batchTag " + batchTag + " failed", e);
      return Optional.empty();
    }
  }

  private Optional<String> getHeader(Response response, String header) {
    Collection<String> headerStrings = response.headers().get(header);
    String headerString = headerStrings.iterator().next();
    return (!StringUtils.equals(EMPTY_HEADER, headerString))
        ? Optional.of(headerString)
        : Optional.empty();
  }
}
