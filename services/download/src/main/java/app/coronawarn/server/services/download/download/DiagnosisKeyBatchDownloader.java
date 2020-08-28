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
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


/**
 * The BatchDownloader downloads the batches containing the keys.
 */
@Service
public class DiagnosisKeyBatchDownloader {

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
   * @return DiagnosisKeyBatchContainer
   */
  public Optional<DiagnosisKeyBatchContainer> downloadBatch(LocalDate date) {
    try (Response response = federationGatewayClient.getDiagnosisKeys(
        "application/protobuf; version=1.0",
        "abcd",
        "C=PL",
        date.format(DateTimeFormatter.ISO_LOCAL_DATE))) {

      String batchTag = getHeader(response, "batchTag");
      String nextBatchTag = getHeader(response, "nextBatchTag");

      InputStream is = response.body().asInputStream();
      DiagnosisKeyBatch diagnosisKeyBatch = DiagnosisKeyBatch.parseFrom(is);
      return Optional.of(new DiagnosisKeyBatchContainer(diagnosisKeyBatch, batchTag, nextBatchTag, date));
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  /**
   * Downloads the batch specified for this date.
   *
   * @param date the date for which the batch should be downloaded
   * @return DiagnosisKeyBatchContainer
   */
  public Optional<DiagnosisKeyBatchContainer> downloadBatch(LocalDate date, String batchTag) {
    try (Response response = federationGatewayClient.getDiagnosisKeys(
        "application/protobuf; version=1.0",
        "abcd",
        "C=PL",
        batchTag,
        date.format(DateTimeFormatter.ISO_LOCAL_DATE))) {

      String nextBatchTag = getHeader(response, "nextBatchTag");

      InputStream is = response.body().asInputStream();
      DiagnosisKeyBatch diagnosisKeyBatch = DiagnosisKeyBatch.parseFrom(is);
      return Optional.of(new DiagnosisKeyBatchContainer(diagnosisKeyBatch, batchTag, nextBatchTag, date));
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  private String getHeader(Response response, String header) {
    Collection<String> headerStrings = response.headers().get(header);
    if (headerStrings != null) {
      return headerStrings.iterator().next();
    }
    return null;
  }

}
