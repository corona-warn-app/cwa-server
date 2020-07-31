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

package app.coronawarn.server.services.federationdownload.runner;


import app.coronawarn.server.common.persistence.domain.FederationBatchDownload;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.FederationBatchDownloadService;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.federationdownload.Application;
import app.coronawarn.server.services.federationdownload.download.BatchDownloader;
import feign.Response;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;


/**
 * This runner retrieves diagnosis key batches.
 */
@Component
@Order(1)
public class Download implements ApplicationRunner {

  private static final Logger logger = LoggerFactory
      .getLogger(Download.class);

  private final ApplicationContext applicationContext;
  private final FederationBatchDownloadService federationBatchDownloadService;
  private final DiagnosisKeyService diagnosisKeyService;
  private final BatchDownloader batchDownloader;


  /**
   * Creates an Download, using {@link ApplicationContext}.
   */

  Download(ApplicationContext applicationContext, FederationBatchDownloadService federationBatchDownloadService,
           DiagnosisKeyService diagnosisKeyService, BatchDownloader batchDownloader) {
    this.applicationContext = applicationContext;
    this.federationBatchDownloadService = federationBatchDownloadService;
    this.diagnosisKeyService = diagnosisKeyService;
    this.batchDownloader = batchDownloader;
  }

  @Override
  public void run(ApplicationArguments args) {
    try {
      List<FederationBatchDownload> federationBatchDownloads =
          federationBatchDownloadService.getFederationBatchDownloads();

      for (FederationBatchDownload federationBatchDownload : federationBatchDownloads) {
        Response result = this.batchDownloader.downloadBatch(federationBatchDownload);
        DiagnosisKeyBatch diagnosisKeyBatch = DiagnosisKeyBatch.parseFrom(result.body().asInputStream());
        //DiagnosisKey diagnosisKey = diagnosisKeyBatch.getKeys(1);
        persistDiagnosisKeysPayload(diagnosisKeyBatch);
      }


    } catch (Exception e) {
      logger.error("Download of diagnosis key batch failed.", e);
      Application.killApplication(applicationContext);
    }
    logger.debug("Batch successfully downloaded.");
  }

  /**
   * Persists the diagnosis keys contained in the specified request payload.
   *
   * @param diagnosisKeyBatch Diagnosis keys that were specified in the request.
   * @throws IllegalArgumentException in case the given collection contains {@literal null}.
   */
  public void persistDiagnosisKeysPayload(DiagnosisKeyBatch diagnosisKeyBatch) {
    List<DiagnosisKey> protoBufferKeysList = diagnosisKeyBatch.getKeysList();
    List<app.coronawarn.server.common.persistence.domain.DiagnosisKey> diagnosisKeys = new ArrayList<>();

    for (DiagnosisKey protoBufferKey : protoBufferKeysList) {
      app.coronawarn.server.common.persistence.domain.FederationDiagnosisKey
          diagnosisKey = app.coronawarn.server.common.persistence.domain.FederationDiagnosisKey
          .builder()
          .fromProtoBuf(protoBufferKey)
          .build();
    }

    diagnosisKeyService.saveDiagnosisKeys(diagnosisKeys);
  }
}
