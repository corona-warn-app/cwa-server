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
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.common.persistence.service.common.ExpirationPolicy;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import app.coronawarn.server.common.persistence.service.common.ValidDiagnosisKeyFilter;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class FederationUploadKeyService {

  private final FederationUploadKeyRepository keyRepository;
  private final ValidDiagnosisKeyFilter validationFilter;
  private final KeySharingPoliciesChecker sharingPoliciesChecker;

  private static final Logger logger = LoggerFactory.getLogger(FederationUploadKeyService.class);

  /**
   * Constructs the key upload service.
   */
  public FederationUploadKeyService(FederationUploadKeyRepository keyRepository, ValidDiagnosisKeyFilter filter,
      KeySharingPoliciesChecker sharingPoliciesChecker) {
    this.keyRepository = keyRepository;
    this.validationFilter = filter;
    this.sharingPoliciesChecker = sharingPoliciesChecker;
  }

  /**
   * Returns all valid persisted diagnosis keys which are ready to be uploaded to the external Federation Gateway
   * service. Readiness of keys means:
   *
   * <p><li> Consent is given by the user (this should always be the case for keys in this table,
   * but a safety check is performed anyway
   * <li> Key is expired conforming to the given policy
   */
  public List<FederationUploadKey> getPendingUploadKeys(ExpirationPolicy policy) {
    AtomicInteger keysPicked = new AtomicInteger();
    AtomicInteger keysPickedAfterConsent = new AtomicInteger();
    AtomicInteger keysPickedAfterValidity = new AtomicInteger();
    AtomicInteger keysPickedAfterSharePolicy = new AtomicInteger();

    var listOfKeys = createStreamFromIterator(keyRepository.findAllUploadableKeys().iterator())
        .peek(k -> keysPicked.addAndGet(1))
        .filter(DiagnosisKey::isConsentToFederation)
        .peek(k -> keysPickedAfterConsent.addAndGet(1))
        .filter(validationFilter::isDiagnosisKeyValid)
        .peek(k -> keysPickedAfterValidity.addAndGet(1))
        .filter(key -> sharingPoliciesChecker.canShareKeyAtTime(key, policy, LocalDateTime.now(UTC)))
        .peek(k -> keysPickedAfterSharePolicy.addAndGet(1))
        .collect(Collectors.toList());

    logger.info("{} keys picked after read from upload table", keysPicked.get());
    logger.info("{} keys remaining after filtering by consent", keysPickedAfterConsent.get());
    logger.info("{} keys remaining after filtering by validity", keysPickedAfterValidity.get());
    logger.info("{} keys remaining after filtering by share policy", keysPickedAfterSharePolicy.get());

    return listOfKeys;
  }

  /**
   * Updates only the batchTagId field of all given upload keys. The entities are not merged
   * with the persisted ones, thus no other side effects are to be expected.
   */
  @Transactional
  public void updateBatchTagForKeys(Collection<FederationUploadKey> originalKeys, String batchTagId) {
    originalKeys.forEach(key -> keyRepository.updateBatchTag(key.getKeyData(), batchTagId));
  }
}
