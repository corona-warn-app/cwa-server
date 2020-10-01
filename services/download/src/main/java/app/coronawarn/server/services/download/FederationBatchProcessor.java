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

import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.*;
import static java.util.stream.Collectors.toList;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.download.normalization.FederationKeyNormalizer;
import app.coronawarn.server.services.download.validation.ValidFederationKeyFilter;
import java.time.LocalDate;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Responsible for downloading and storing batch information from the federation gateway.
 */
@Component
public class FederationBatchProcessor {

  private static final Logger logger = LoggerFactory.getLogger(FederationBatchProcessor.class);
  private final FederationBatchInfoService batchInfoService;
  private final DiagnosisKeyService diagnosisKeyService;
  private final FederationGatewayDownloadService federationGatewayDownloadService;
  private final DownloadServiceConfig config;
  private final ValidFederationKeyFilter validFederationKeyFilter;

  /**
   * Constructor.
   *
   * @param batchInfoService                 A {@link FederationBatchInfoService} for accessing diagnosis key batch
   *                                         information.
   * @param diagnosisKeyService              A {@link DiagnosisKeyService} for storing retrieved diagnosis keys.
   * @param federationGatewayDownloadService A {@link FederationGatewayDownloadService} for retrieving federation
   *                                         diagnosis key batches.
   * @param config                           A {@link DownloadServiceConfig} for retrieving federation configuration.
   * @param federationKeyValidator           A {@link ValidFederationKeyFilter} for validating keys in the downloaded
   *                                         batches
   */
  public FederationBatchProcessor(FederationBatchInfoService batchInfoService,
      DiagnosisKeyService diagnosisKeyService, FederationGatewayDownloadService federationGatewayDownloadService,
      DownloadServiceConfig config, ValidFederationKeyFilter federationKeyValidator) {
    this.batchInfoService = batchInfoService;
    this.diagnosisKeyService = diagnosisKeyService;
    this.federationGatewayDownloadService = federationGatewayDownloadService;
    this.config = config;
    this.validFederationKeyFilter = federationKeyValidator;
  }

  /**
   * Stores the batch info for the specified date. Its status is set to {@link FederationBatchStatus#UNPROCESSED}.
   *
   * @param date The date for which the first batch info is stored.
   */
  public void saveFirstBatchInfoForDate(LocalDate date) {
    try {
      logger.info("Triggering download of first batch for date {}.", date);
      BatchDownloadResponse response = federationGatewayDownloadService.downloadBatch(date);
      batchInfoService.save(new FederationBatchInfo(response.getBatchTag(), date));
    } catch (Exception e) {
      logger.error("Triggering download of first batch for date {} failed.", date, e);
    }
  }

  /**
   * Downloads and processes all batches from the federation gateway that have previously been marked with the status
   * value {@link FederationBatchStatus#ERROR}.
   */
  public void processErrorFederationBatches() {
    List<FederationBatchInfo> federationBatchInfosWithError = batchInfoService.findByStatus(ERROR);
    logger.info("{} error federation batches for reprocessing found", federationBatchInfosWithError.size());
    federationBatchInfosWithError.forEach(this::retryProcessingBatch);
  }

  private void retryProcessingBatch(FederationBatchInfo federationBatchInfo) {
    try {
      processBatchAndReturnNextBatchId(federationBatchInfo, ERROR_WONT_RETRY)
          .ifPresent(nextBatchTag ->
              batchInfoService.save(new FederationBatchInfo(nextBatchTag, federationBatchInfo.getDate())));
    } catch (Exception e) {
      logger.error("Failed to save next federation batch info for processing. Will not try again.", e);
      batchInfoService.updateStatus(federationBatchInfo, ERROR_WONT_RETRY);
    }
  }

  /**
   * Downloads and processes all batches from the federation gateway that have previously been marked with status value
   * {@link FederationBatchStatus#UNPROCESSED}.
   */
  public void processUnprocessedFederationBatches() {
    Deque<FederationBatchInfo> unprocessedBatches = new LinkedList<>(batchInfoService.findByStatus(UNPROCESSED));
    logger.info("{} unprocessed federation batches found", unprocessedBatches.size());

    while (!unprocessedBatches.isEmpty()) {
      FederationBatchInfo currentBatch = unprocessedBatches.remove();
      processBatchAndReturnNextBatchId(currentBatch, ERROR)
          .ifPresent(nextBatchTag ->
              unprocessedBatches.add(new FederationBatchInfo(nextBatchTag, currentBatch.getDate())));
    }
  }

  private Optional<String> processBatchAndReturnNextBatchId(
      FederationBatchInfo batchInfo, FederationBatchStatus errorStatus) {
    LocalDate date = batchInfo.getDate();
    String batchTag = batchInfo.getBatchTag();
    logger.info("Processing batch for date {} and batchTag {}", date, batchTag);
    AtomicBoolean batchContainsInvalidKeys = new AtomicBoolean(false);
    try {
      BatchDownloadResponse response = federationGatewayDownloadService.downloadBatch(batchTag, date);
      response.getDiagnosisKeyBatch().ifPresent(diagnosisKeyBatch -> {
        int batchKeysCount = diagnosisKeyBatch.getKeysCount();
        logger.info("Downloaded {} keys for date {} and batchTag {}", batchKeysCount, date, batchTag);
        List<DiagnosisKey> validDiagnosisKeys = extractValidDiagnosisKeys(diagnosisKeyBatch);
        int skippedKeys = batchKeysCount - validDiagnosisKeys.size();
        if (skippedKeys > 0) {
          batchContainsInvalidKeys.set(true);
          logger.info("{} keys failed validation and were skipped", skippedKeys);
        }
        int insertedKeys = diagnosisKeyService.saveDiagnosisKeys(validDiagnosisKeys);
        logger.info("Successfully inserted {} keys for date {} and batchTag {}", insertedKeys, date, batchTag);
      });
      batchInfoService.updateStatus(batchInfo, batchContainsInvalidKeys.get() ? PROCESSED_WITH_FAILURES : PROCESSED);
      return response.getNextBatchTag();
    } catch (Exception e) {
      logger.error("Federation batch processing for date {} and batchTag {} failed. Status set to {}",
          date, batchTag, errorStatus.name(), e);
      batchInfoService.updateStatus(batchInfo, errorStatus);
      return Optional.empty();
    }
  }

  private List<DiagnosisKey> extractValidDiagnosisKeys(DiagnosisKeyBatch diagnosisKeyBatch) {
    return diagnosisKeyBatch.getKeysList()
        .stream()
        .filter(validFederationKeyFilter::isValid)
        .map(diagnosisKey -> {
          try {
            return Optional.of(DiagnosisKey.builder().fromFederationDiagnosisKey(diagnosisKey)
                .withFieldNormalization(new FederationKeyNormalizer(config))
                .build());
          } catch (Exception ex) {
            logger.info("Building key failed");
            return Optional.<DiagnosisKey>empty();
          }
        })
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(toList());
  }
}
