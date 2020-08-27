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


import app.coronawarn.server.common.persistence.domain.FederationBatch;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.FederationBatchService;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.download.download.DiagnosisKeyBatchContainer;
import app.coronawarn.server.services.download.download.DiagnosisKeyBatchDownloader;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


/**
 * This runner retrieves diagnosis key batches.
 */
@Component
@Order(1)
public class Download implements ApplicationRunner {

  private static final Logger logger = LoggerFactory
      .getLogger(Download.class);

  private final ApplicationContext applicationContext;
  private final FederationBatchService federationBatchService;
  private final DiagnosisKeyService diagnosisKeyService;
  private final DiagnosisKeyBatchDownloader diagnosisKeyBatchDownloader;

  /**
   * Creates a Download, using {@link ApplicationContext}.
   */
  Download(ApplicationContext applicationContext, FederationBatchService federationBatchService,
           DiagnosisKeyService diagnosisKeyService, DiagnosisKeyBatchDownloader diagnosisKeyBatchDownloader) {
    this.applicationContext = applicationContext;
    this.federationBatchService = federationBatchService;
    this.diagnosisKeyService = diagnosisKeyService;
    this.diagnosisKeyBatchDownloader = diagnosisKeyBatchDownloader;
  }

  @Override
  public void run(ApplicationArguments args) {
    // TODO UTC handling?
    LocalDate yesterday = LocalDate.now(); //LocalDate.now().minus(Period.ofDays(1));
    saveFirstBatchTagForDate(yesterday);

    // fetch federation batches with error state
    // store nextbatchtag, store their diagnosis keys, set status to processed
    // in case of error: set status to error_wont_fix

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

  private void saveFirstBatchTagForDate(LocalDate date) {
    Optional<DiagnosisKeyBatchContainer> diagnosisKeyBatchContainerOptional =
        diagnosisKeyBatchDownloader.downloadBatch(date);

    if (diagnosisKeyBatchContainerOptional.isEmpty()) {
      return;
    }

    DiagnosisKeyBatchContainer diagnosisKeyBatchContainer = diagnosisKeyBatchContainerOptional.get();
    federationBatchService.saveFederationBatch(new FederationBatch(diagnosisKeyBatchContainer.getBatchTag(), date));
  }

  private void downloadBatch(LocalDate date) {
    Optional<DiagnosisKeyBatchContainer> diagnosisKeyBatchContainerOptional =
        diagnosisKeyBatchDownloader.downloadBatch(date);

    if (diagnosisKeyBatchContainerOptional.isEmpty()) {
      return;
    }

    DiagnosisKeyBatchContainer diagnosisKeyBatchContainer = diagnosisKeyBatchContainerOptional.get();



    // TODO check if already in table before processing
    //federationBatchService.saveFederationBatch(new FederationBatch(batchTag, date, FederationBatchStatus.PROCESSED));
    storeDiagnosisKeyBatch(diagnosisKeyBatchContainer.getDiagnosisKeyBatch());
    String nextBatchTag = diagnosisKeyBatchContainer.getNextBatchTag();

    if (!StringUtils.isEmpty(nextBatchTag)) {
      federationBatchService.saveFederationBatch(new FederationBatch(nextBatchTag, date));
    }
  }

  private void storeDiagnosisKeyBatch(DiagnosisKeyBatch diagnosisKeyBatch) {
    List<DiagnosisKey> keys = diagnosisKeyBatch.getKeysList();
    // TODO
  }
}
