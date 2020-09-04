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

import static java.time.ZoneOffset.UTC;
import static org.springframework.data.util.StreamUtils.createStreamFromIterator;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.common.persistence.service.common.DiagnosisKeyExpirationChecker;
import app.coronawarn.server.common.persistence.service.common.ExpirationPolicy;
import app.coronawarn.server.common.persistence.service.common.ValidDiagnosisKeyFilter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class FederationUploadKeyService {

  private static final Logger logger = LoggerFactory.getLogger(FederationUploadKeyService.class);

  private final FederationUploadKeyRepository keyRepository;
  private final ValidDiagnosisKeyFilter validationFilter;
  private final DiagnosisKeyExpirationChecker expirationChecker;

  /**
   * Constructs the key upload service.
   */
  public FederationUploadKeyService(FederationUploadKeyRepository keyRepository, ValidDiagnosisKeyFilter filter,
      DiagnosisKeyExpirationChecker expirationChecker) {
    this.keyRepository = keyRepository;
    this.validationFilter = filter;
    this.expirationChecker = expirationChecker;
  }

  /**
   * Returns all valid persisted diagnosis keys which are ready to be uploaded to the external Federation Gateway
   * service. Readiness of keys means:
   *
   * <p><li> Consent is given by the user (this should always be the case for keys in this table,
   * but a safety check is performed anyway
   * <li> Key is expired conforming to the given policy
   */
  public List<DiagnosisKey> getPendingUploadKeys(ExpirationPolicy policy) {
    return createStreamFromIterator(
           keyRepository.findAllUploadableKeys().iterator())
           .filter(DiagnosisKey::isConsentToFederation)
           .filter(this::isKeyValid)
           .filter(key -> expirationChecker.canShareKeyAtTime(key, policy, LocalDateTime.now(UTC)))
           .collect(Collectors.toList());
  }

  private boolean isKeyValid(DiagnosisKey key) {
    boolean isValid = false;
    try {
      isValid = validationFilter.isDiagnosisKeyValid(key);
    } catch  (InvalidDiagnosisKeyException e) {
      // log and allow collection of other keys by ignoring the runtime exception
      logger.debug("Invalid key found in pending upload table. {}", e.getMessage());
    }
    return isValid;
  }
}
