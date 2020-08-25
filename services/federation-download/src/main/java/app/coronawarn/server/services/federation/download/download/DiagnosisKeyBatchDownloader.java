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

package app.coronawarn.server.services.federation.download.download;

import app.coronawarn.server.services.federation.download.config.FederationDownloadServiceConfig;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


/**
 * The BatchDownloader downloads the batches containing the keys.
 */
@Service
public class DiagnosisKeyBatchDownloader implements DiagnosisKeyBatchDownloaders {

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyBatchDownloader.class);

  private final FederationDownloadServiceConfig federationDownloadServiceConfig;

  public DiagnosisKeyBatchDownloader(FederationDownloadServiceConfig federationDownloadServiceConfig) {
    this.federationDownloadServiceConfig = federationDownloadServiceConfig;
  }

  @Override
  public DiagnosisKeyBatchContainer downloadBatch(LocalDate date) {
    return null;
  }

  @Override
  public DiagnosisKeyBatchContainer downloadBatch(LocalDate date, String batchTag) {
    return null;
  }

}
