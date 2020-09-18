

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
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class FederationUploadKeyService {

  private final FederationUploadKeyRepository keyRepository;
  private final ValidDiagnosisKeyFilter validationFilter;
  private final KeySharingPoliciesChecker sharingPoliciesChecker;

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
    return createStreamFromIterator(
           keyRepository.findAllUploadableKeys().iterator())
           .filter(DiagnosisKey::isConsentToFederation)
           .filter(validationFilter::isDiagnosisKeyValid)
           .filter(key -> sharingPoliciesChecker.canShareKeyAtTime(key, policy, LocalDateTime.now(UTC)))
           .collect(Collectors.toList());
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
