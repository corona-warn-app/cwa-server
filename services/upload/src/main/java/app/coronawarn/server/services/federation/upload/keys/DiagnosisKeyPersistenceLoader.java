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

package app.coronawarn.server.services.federation.upload.keys;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.FederationUploadKeyService;
import app.coronawarn.server.common.persistence.service.common.ExpirationPolicy;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DiagnosisKeyPersistenceLoader implements DiagnosisKeyLoader {

  private final FederationUploadKeyService uploadKeyService;
  private final UploadServiceConfig uploadConfig;

  public DiagnosisKeyPersistenceLoader(FederationUploadKeyService uploadKeyService,
      UploadServiceConfig uploadConfig) {
    this.uploadKeyService = uploadKeyService;
    this.uploadConfig = uploadConfig;
  }

  @Override
  public List<DiagnosisKey> loadDiagnosisKeys() {
    return this.uploadKeyService
        .getPendingUploadKeys(ExpirationPolicy.of(uploadConfig.getExpiryPolicyMinutes(), ChronoUnit.MINUTES));
  }
}
