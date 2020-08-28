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
import static org.apache.commons.lang3.StringUtils.isBlank;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationBatch;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.FederationBatchService;
import app.coronawarn.server.services.download.download.DiagnosisKeyBatchContainer;
import app.coronawarn.server.services.download.download.DiagnosisKeyBatchDownloader;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
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

  private static final Logger logger = LoggerFactory
      .getLogger(Download.class);

  private final FederationBatchService federationBatchService;
  private final DiagnosisKeyService diagnosisKeyService;
  private final DiagnosisKeyBatchDownloader diagnosisKeyBatchDownloader;

  /**
   * Creates a Download.
   */
  Download(FederationBatchService federationBatchService,
      DiagnosisKeyService diagnosisKeyService, DiagnosisKeyBatchDownloader diagnosisKeyBatchDownloader) {
    this.federationBatchService = federationBatchService;
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
    Optional<DiagnosisKeyBatchContainer> diagnosisKeyBatchContainerOptional =
        diagnosisKeyBatchDownloader.downloadBatch(date);

    if (diagnosisKeyBatchContainerOptional.isPresent()) {
      DiagnosisKeyBatchContainer diagnosisKeyBatchContainer = diagnosisKeyBatchContainerOptional.get();
      federationBatchService.saveFederationBatch(new FederationBatch(diagnosisKeyBatchContainer.getBatchTag(), date));
    }
  }

  private void processErrorFederationBatches() {
    List<FederationBatch> federationBatchesWithErrors = federationBatchService.findByStatus(ERROR);
    federationBatchesWithErrors.forEach(this::retryProcessingBatch);
  }

  private void retryProcessingBatch(FederationBatch federationBatch) {
    try {
      Optional<DiagnosisKeyBatchContainer> diagnosisKeyBatchContainerOptional =
          diagnosisKeyBatchDownloader.downloadBatch(federationBatch.getDate(), federationBatch.getBatchTag());
      DiagnosisKeyBatchContainer diagnosisKeyBatchContainer = diagnosisKeyBatchContainerOptional.orElseThrow();

      saveNextBatchTag(diagnosisKeyBatchContainer, federationBatch.getDate());

      diagnosisKeyService.saveDiagnosisKeys(convertDiagnosisKeys(diagnosisKeyBatchContainer));
      federationBatchService.updateStatus(federationBatch, PROCESSED);
    } catch (Exception e) {
      logger.error("Retry of federation batch processing failed. Will not try again.", e);
      federationBatchService.updateStatus(federationBatch, ERROR_WONT_RETRY);
    }
  }

  private List<DiagnosisKey> convertDiagnosisKeys(DiagnosisKeyBatchContainer diagnosisKeyBatchContainer) {
    return diagnosisKeyBatchContainer.getDiagnosisKeyBatch().getKeysList()
        .stream()
        .map(diagnosisKey -> DiagnosisKey.builder().fromFederationDiagnosisKey(diagnosisKey).build())
        .collect(Collectors.toList());
  }

  private void saveNextBatchTag(DiagnosisKeyBatchContainer diagnosisKeyBatchContainer, LocalDate batchDate) {
    if (!isBlank(diagnosisKeyBatchContainer.getNextBatchTag())) {
      federationBatchService.saveFederationBatch(
          new FederationBatch(diagnosisKeyBatchContainer.getNextBatchTag(), batchDate));
    }
  }


  private void processFederationBatches() {
    // fetch one unprocessed (and no error state) federation batch
    // store nextbatchTag, store diagnosis keys, update status to processed
    // in case of error: set status to error
    // loop

    /*
    FederationBatch batchToProcess = federationBatchService.getNextFederationBatchToProcess();
    while (batchToProcess != null) {
      downloadBatch(batchToProcess);
      batchToProcess = federationBatchService.getNextFederationBatchToProcess();
    }
     */
  }

}
