

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
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("connect-efgs | connect-chgs")
public class FederationUploadKeyService {

  private final FederationUploadKeyRepository keyRepository;
  private final ValidDiagnosisKeyFilter validationFilter;
  private final KeySharingPoliciesChecker sharingPoliciesChecker;

  private static final Logger logger = LoggerFactory.getLogger(FederationUploadKeyService.class);

  /**
   * Constructs the key upload service.
   *
   * @param keyRepository key repo {@link FederationUploadKeyRepository}
   * @param filter key filter {@link ValidDiagnosisKeyFilter}
   * @param sharingPoliciesChecker sharing policy validation {@link KeySharingPoliciesChecker}
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
   * Consent is given by the user (this should always be the case for keys in this table,
   * but a safety check is performed anyway
   * Key is expired conforming to the given policy
   *
   * @param policy the ExpirationPolicy
   * @param daysToRetain number of days to retain
   * @return a list of keys
   */
  public List<FederationUploadKey> getPendingUploadKeys(ExpirationPolicy policy, int daysToRetain) {
    AtomicInteger keysPicked = new AtomicInteger();
    AtomicInteger keysPickedAfterConsent = new AtomicInteger();
    AtomicInteger keysPickedAfterValidity = new AtomicInteger();
    AtomicInteger keysPickedAfterSharePolicy = new AtomicInteger();
    AtomicInteger keysPickedAfterRetentionPolicy = new AtomicInteger();

    var listOfKeys = createStreamFromIterator(keyRepository.findAllUploadableKeys().iterator())
        .peek(k -> keysPicked.addAndGet(1))
        .filter(DiagnosisKey::isConsentToFederation)
        .peek(k -> keysPickedAfterConsent.addAndGet(1))
        .filter(validationFilter::isDiagnosisKeyValid)
        .peek(k -> keysPickedAfterValidity.addAndGet(1))
        .filter(key -> sharingPoliciesChecker.canShareKeyAtTime(key, policy, LocalDateTime.now(UTC)))
        .peek(k -> keysPickedAfterSharePolicy.addAndGet(1))
        .filter(key -> key.isYoungerThanRetentionThreshold(daysToRetain))
        .peek(k -> keysPickedAfterRetentionPolicy.addAndGet(1))
        .collect(Collectors.toList());
    logger.info("Keys selected for upload: {}", listOfKeys.size());

    logger.info("{} keys picked after read from upload table", keysPicked.get());
    logger.info("{} keys remaining after filtering by consent", keysPickedAfterConsent.get());
    logger.info("{} keys remaining after filtering by validity", keysPickedAfterValidity.get());
    logger.info("{} keys remaining after filtering by share policy", keysPickedAfterSharePolicy.get());
    logger.info("{} keys remaining after filtering by retention policy ({} days)",
        keysPickedAfterRetentionPolicy.get(),
        daysToRetain);

    return listOfKeys;
  }

  /**
   * Updates only the batchTagId field of all given upload keys. The entities are not merged with the persisted ones,
   * thus no other side effects are to be expected.
   *
   * @param originalKeys a app.coronawarn.server.common.shared.collection of FederationUploadKey
   * @param batchTagId the id for the keys
   */
  @Transactional
  public void updateBatchTagForKeys(Collection<FederationUploadKey> originalKeys, String batchTagId) {
    originalKeys.forEach(key -> keyRepository.updateBatchTag(key.getKeyData(), batchTagId));
  }
}
