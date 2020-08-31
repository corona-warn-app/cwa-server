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

package app.coronawarn.server.services.download.runner;

import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.ERROR;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.ERROR_WONT_RETRY;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.PROCESSED;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.services.download.download.DiagnosisKeyBatchDownloader;
import app.coronawarn.server.services.download.download.FederationGatewayResponse;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * This runner retrieves diagnosis key batches.
 */
@Component
@Order(1)
public class Download implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(Download.class);
  private final FederationBatchInfoService federationBatchInfoService;
  private final DiagnosisKeyService diagnosisKeyService;
  private final DiagnosisKeyBatchDownloader diagnosisKeyBatchDownloader;

  /**
   * Creates a Download.
   */
  Download(FederationBatchInfoService federationBatchInfoService,
      DiagnosisKeyService diagnosisKeyService, DiagnosisKeyBatchDownloader diagnosisKeyBatchDownloader) {
    this.federationBatchInfoService = federationBatchInfoService;
    this.diagnosisKeyService = diagnosisKeyService;
    this.diagnosisKeyBatchDownloader = diagnosisKeyBatchDownloader;
  }

  @Override
  public void run(ApplicationArguments args) {
    LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minus(Period.ofDays(1));
    saveFirstBatchTagForDate(yesterday);
    processErrorFederationBatches();
    processFederationBatches();
  }

  private void saveFirstBatchTagForDate(LocalDate date) {
    Optional<FederationGatewayResponse> diagnosisKeyBatchContainerOptional =
        diagnosisKeyBatchDownloader.downloadBatch(date);

    if (diagnosisKeyBatchContainerOptional.isPresent()) {
      FederationGatewayResponse federationGatewayResponse = diagnosisKeyBatchContainerOptional.get();
      federationBatchInfoService
          .save(new FederationBatchInfo(federationGatewayResponse.getBatchTag(), date));
    }
  }

  private void processErrorFederationBatches() {
    List<FederationBatchInfo> federationBatchInfosWithError = federationBatchInfoService.findByStatus(ERROR);
    federationBatchInfosWithError.forEach(this::retryProcessingBatch);
  }

  private void retryProcessingBatch(FederationBatchInfo federationBatchInfo) {
    try {
      processBatchAndReturnNextBatchId(federationBatchInfo, ERROR_WONT_RETRY)
          .ifPresent(batchTag -> {
            FederationBatchInfo nextBatchInfo = new FederationBatchInfo(batchTag, federationBatchInfo.getDate());
            federationBatchInfoService.save(nextBatchInfo);
          });
    } catch (Exception e) {
      logger.error("Retry of federation batch processing failed. Will not try again.", e);
      federationBatchInfoService.updateStatus(federationBatchInfo, ERROR_WONT_RETRY);
    }
  }

  private void processFederationBatches() {
    Deque<FederationBatchInfo> unprocessedBatches =
        new LinkedList<>(federationBatchInfoService.findByStatus(FederationBatchStatus.UNPROCESSED));

    while (!unprocessedBatches.isEmpty()) {
      FederationBatchInfo currentBatch = unprocessedBatches.remove();
      processBatchAndReturnNextBatchId(currentBatch, ERROR)
          .ifPresent(nextBatchTag ->
              unprocessedBatches.add(new FederationBatchInfo(nextBatchTag, currentBatch.getDate())));
    }
  }

  private Optional<String> processBatchAndReturnNextBatchId(
      FederationBatchInfo currentBatch, FederationBatchStatus errorStatus) {
    try {
      FederationGatewayResponse diagnosisKeyBatchContainer =
          diagnosisKeyBatchDownloader.downloadBatch(currentBatch.getDate(), currentBatch.getBatchTag()).orElseThrow();

      diagnosisKeyService.saveDiagnosisKeys(convertDiagnosisKeys(diagnosisKeyBatchContainer));
      federationBatchInfoService.updateStatus(currentBatch, PROCESSED);
      return diagnosisKeyBatchContainer.getNextBatchTag();
    } catch (Exception e) {
      logger.error("Federation batch processing failed. Status set to {}", e, errorStatus.name());
      federationBatchInfoService.updateStatus(currentBatch, errorStatus);
      return Optional.empty();
    }
  }

  private List<DiagnosisKey> convertDiagnosisKeys(FederationGatewayResponse federationGatewayResponse) {
    return federationGatewayResponse.getDiagnosisKeyBatch().getKeysList()
        .stream()
        .map(diagnosisKey -> DiagnosisKey.builder().fromFederationDiagnosisKey(diagnosisKey).build())
        .collect(Collectors.toList());
  }
}
