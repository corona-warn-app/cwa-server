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

package app.coronawarn.server.common.persistence.service;

import static org.springframework.data.util.StreamUtils.createStreamFromIterator;

import app.coronawarn.server.common.persistence.domain.FederationBatchDownload;
import app.coronawarn.server.common.persistence.repository.FederationBatchDownloadRepository;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FederationBatchDownloadService {

  private static final Logger logger = LoggerFactory.getLogger(FederationBatchDownloadService.class);
  private final FederationBatchDownloadRepository federationBatchDownloadRepository;

  public FederationBatchDownloadService(FederationBatchDownloadRepository federationBatchDownloadRepository) {
    this.federationBatchDownloadRepository = federationBatchDownloadRepository;
  }

  /**
   * Persists the {@link FederationBatchDownload} instance. If the data of a particular federation batch
   * already exists in the database, this federation batch is not persisted.
   *
   * @param federationBatchDownload must not contain {@literal null}.
   */
  @Timed
  @Transactional
  public void saveFederationBatchDownload(FederationBatchDownload federationBatchDownload) {
    federationBatchDownloadRepository
        .saveDoNothingOnConflict(federationBatchDownload.getBatchTag(), federationBatchDownload.getDate());
  }

  /**
   * Returns all valid persisted federation batches, sorted by their submission timestamp.
   */
  public List<FederationBatchDownload> getFederationBatchDownloads() {
    List<FederationBatchDownload> federationBatchDownloads = createStreamFromIterator(
        federationBatchDownloadRepository.findAll(Sort.by(Direction.ASC, "date")).iterator())
        .collect(Collectors.toList());

    logger.info("Retrieved {} federation batch(es).",
        federationBatchDownloads.size());

    return federationBatchDownloads;
  }


  //Todo delete batches
}
