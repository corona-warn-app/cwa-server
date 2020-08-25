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

package app.coronawarn.server.services.federation.download.runner;


import app.coronawarn.server.common.persistence.domain.FederationBatch;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.FederationBatchService;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.federation.download.download.DiagnosisKeyBatchContainer;
import app.coronawarn.server.services.federation.download.download.DiagnosisKeyBatchDownloaders;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
  private final DiagnosisKeyBatchDownloaders diagnosisKeyBatchDownloader;

  /**
   * Creates a Download, using {@link ApplicationContext}.
   */
  Download(ApplicationContext applicationContext, FederationBatchService federationBatchService,
           DiagnosisKeyService diagnosisKeyService, DiagnosisKeyBatchDownloaders diagnosisKeyBatchDownloader) {
    this.applicationContext = applicationContext;
    this.federationBatchService = federationBatchService;
    this.diagnosisKeyService = diagnosisKeyService;
    this.diagnosisKeyBatchDownloader = diagnosisKeyBatchDownloader;
  }

  @Override
  public void run(ApplicationArguments args) {
    // run();
  }

  private void run() {
    LocalDate yesterday = LocalDate.from(Instant.now().minus(1, ChronoUnit.DAYS));
    downloadBatch(yesterday);

    FederationBatch batchToProcess = federationBatchService.getNextFederationBatchToProcess();
    while (batchToProcess != null) {
      downloadBatch(batchToProcess);
      batchToProcess = federationBatchService.getNextFederationBatchToProcess();
    }
  }

  private void downloadBatch(LocalDate date) {
    DiagnosisKeyBatchContainer diagnosisKeyBatchContainer = diagnosisKeyBatchDownloader.downloadBatch(date);

    if (diagnosisKeyBatchContainer == null) {
      return;
    }

    storeDiagnosisKeyBatch(diagnosisKeyBatchContainer.getDiagnosisKeyBatch());
    String nextBatchTag = diagnosisKeyBatchContainer.getNextBatchTag();

    if (!StringUtils.isEmpty(nextBatchTag)) {
      federationBatchService.saveFederationBatch(new FederationBatch(nextBatchTag, date));
    }
  }

  private void downloadBatch(FederationBatch federationBatch) {
    DiagnosisKeyBatchContainer diagnosisKeyBatchContainer =
        diagnosisKeyBatchDownloader.downloadBatch(federationBatch.getDate(), federationBatch.getBatchTag());

    if (diagnosisKeyBatchContainer == null) {
      federationBatchService.markFederationBatchWithStatus(federationBatch, FederationBatchStatus.ERROR);
      return;
    }

    storeDiagnosisKeyBatch(diagnosisKeyBatchContainer.getDiagnosisKeyBatch());
    String nextBatchTag = diagnosisKeyBatchContainer.getNextBatchTag();

    if (!StringUtils.isEmpty(nextBatchTag)) {
      federationBatchService.saveFederationBatch(new FederationBatch(nextBatchTag, federationBatch.getDate()));
    }
  }

  private void storeDiagnosisKeyBatch(DiagnosisKeyBatch diagnosisKeyBatch) {
    List<DiagnosisKey> keys = diagnosisKeyBatch.getKeysList();
    // TODO
  }
}
