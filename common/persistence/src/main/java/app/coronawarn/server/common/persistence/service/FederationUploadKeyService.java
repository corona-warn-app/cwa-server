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

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.common.persistence.service.common.ValidDiagnosisKeyFilter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;


@Component
public class FederationUploadKeyService {

  private final FederationUploadKeyRepository keyRepository;
  private final ValidDiagnosisKeyFilter validationFilter;

  public FederationUploadKeyService(FederationUploadKeyRepository keyRepository, ValidDiagnosisKeyFilter filter) {
    this.keyRepository = keyRepository;
    this.validationFilter = filter;
  }

  /**
   * Returns all valid persisted diagnosis keys which are ready to be uploaded
   * to the external Federation Gateway service.
   */
  public List<DiagnosisKey> getPendingUploadKeys() {
    List<DiagnosisKey> diagnosisKeys = createStreamFromIterator(
        keyRepository.findAllUploadableKeys().iterator()).collect(Collectors.toList());
    return validationFilter.filter(diagnosisKeys);
  }
}
