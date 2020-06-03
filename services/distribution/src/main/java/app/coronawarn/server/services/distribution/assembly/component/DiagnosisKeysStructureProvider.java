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

package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.DiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.DiagnosisKeysDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Retrieves stored diagnosis keys and builds a {@link DiagnosisKeysDirectory} with them.
 */
@Component
public class DiagnosisKeysStructureProvider {

  private static final Logger logger = LoggerFactory
      .getLogger(DiagnosisKeysStructureProvider.class);

  private final DiagnosisKeyBundler diagnosisKeyBundler;
  private final DiagnosisKeyService diagnosisKeyService;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Creates a new DiagnosisKeysStructureProvider.
   */
  DiagnosisKeysStructureProvider(DiagnosisKeyService diagnosisKeyService, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig, DiagnosisKeyBundler diagnosisKeyBundler) {
    this.diagnosisKeyService = diagnosisKeyService;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
    this.diagnosisKeyBundler = diagnosisKeyBundler;
  }

  /**
   * Get directory for diagnosis keys from database.
   *
   * @return the directory
   */
  public Directory<WritableOnDisk> getDiagnosisKeys() {
    logger.debug("Querying diagnosis keys from the database...");
    Collection<DiagnosisKey> diagnosisKeys = diagnosisKeyService.getDiagnosisKeys();
    diagnosisKeyBundler.setDiagnosisKeys(diagnosisKeys);
    return new DiagnosisKeysDirectory(diagnosisKeyBundler, cryptoProvider, distributionServiceConfig);
  }
}
