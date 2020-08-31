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

import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.ERROR;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.ERROR_WONT_RETRY;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.PROCESSED;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.UNPROCESSED;
import static java.util.stream.Collectors.toList;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import java.time.LocalDate;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Responsible for downloading and storing diagnosis key batches from the federation gateway.
 */
@Component
public class DiagnosisKeyBatchProcessor {

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyBatchProcessor.class);
  private final FederationBatchInfoService batchInfoService;
  private final DiagnosisKeyService diagnosisKeyService;
  private final DiagnosisKeyBatchDownloader batchDownloader;

  /**
   * Constructor.
   *
   * @param batchInfoService    A {@link FederationBatchInfoService} for accessing diagnosis key batch info instances.
   * @param diagnosisKeyService A {@link DiagnosisKeyService} for storing diagnosis keys from the federation batches.
   * @param batchDownloader     A {@link DiagnosisKeyBatchDownloader} for retrieving federation diagnosis key batches.
   */
  public DiagnosisKeyBatchProcessor(FederationBatchInfoService batchInfoService,
      DiagnosisKeyService diagnosisKeyService, DiagnosisKeyBatchDownloader batchDownloader) {
    this.batchInfoService = batchInfoService;
    this.diagnosisKeyService = diagnosisKeyService;
    this.batchDownloader = batchDownloader;
  }

  /**
   * Stores the batch info of the first diagnosis key batch for the specified date. Its status is set to {@link
   * FederationBatchStatus#UNPROCESSED}.
   *
   * @param date The date for which the first batch info is stored.
   */
  public void saveFirstBatchInfoForDate(LocalDate date) {
    batchDownloader.downloadBatch(date)
        .ifPresent(serverResponse ->
            batchInfoService.save(new FederationBatchInfo(serverResponse.getBatchTag(), date)));
  }

  /**
   * Downloads and processes all diagnosis key batches from the federation gateway that have previously been marked with
   * the status value {@link FederationBatchStatus#ERROR}.
   */
  public void processErrorFederationBatches() {
    List<FederationBatchInfo> federationBatchInfosWithError = batchInfoService.findByStatus(ERROR);
    federationBatchInfosWithError.forEach(this::retryProcessingBatch);
  }

  private void retryProcessingBatch(FederationBatchInfo federationBatchInfo) {
    try {
      processBatchAndReturnNextBatchId(federationBatchInfo, ERROR_WONT_RETRY)
          .ifPresent(batchTag ->
              batchInfoService.save(new FederationBatchInfo(batchTag, federationBatchInfo.getDate())));
    } catch (Exception e) {
      logger.error("Retry of federation batch processing failed. Will not try again.", e);
      batchInfoService.updateStatus(federationBatchInfo, ERROR_WONT_RETRY);
    }
  }

  /**
   * Downloads and processes all diagnosis key batches from the federation gateway that have previously been marked with
   * status value {@link FederationBatchStatus#UNPROCESSED}.
   */
  public void processUnprocessedFederationBatches() {
    Deque<FederationBatchInfo> unprocessedBatches = new LinkedList<>(batchInfoService.findByStatus(UNPROCESSED));

    while (!unprocessedBatches.isEmpty()) {
      FederationBatchInfo currentBatch = unprocessedBatches.remove();
      processBatchAndReturnNextBatchId(currentBatch, ERROR)
          .ifPresent(nextBatchTag ->
              unprocessedBatches.add(new FederationBatchInfo(nextBatchTag, currentBatch.getDate())));
    }
  }

  private Optional<String> processBatchAndReturnNextBatchId(
      FederationBatchInfo batchInfo, FederationBatchStatus errorStatus) {
    try {
      FederationGatewayResponse federationGatewayResponse = batchDownloader
          .downloadBatch(batchInfo.getDate(), batchInfo.getBatchTag())
          .orElseThrow();

      diagnosisKeyService.saveDiagnosisKeys(convertDiagnosisKeys(federationGatewayResponse));
      batchInfoService.updateStatus(batchInfo, PROCESSED);
      return federationGatewayResponse.getNextBatchTag();
    } catch (Exception e) {
      logger.error("Federation batch processing failed. Status set to {}", e, errorStatus.name());
      batchInfoService.updateStatus(batchInfo, errorStatus);
      return Optional.empty();
    }
  }

  private List<DiagnosisKey> convertDiagnosisKeys(FederationGatewayResponse federationGatewayResponse) {
    return federationGatewayResponse.getDiagnosisKeyBatch().getKeysList()
        .stream()
        .map(diagnosisKey -> DiagnosisKey.builder().fromFederationDiagnosisKey(diagnosisKey).build())
        .collect(toList());
  }
}
