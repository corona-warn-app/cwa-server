

package app.coronawarn.server.common.persistence.service;

import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.assertDiagnosisKeysEqual;
import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.buildDiagnosisKeyForSubmissionTimestamp;
import static org.mockito.Mockito.*;

import java.time.temporal.ChronoUnit;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import app.coronawarn.server.common.persistence.service.common.ExpirationPolicy;

@DataJdbcTest
class FederationUploadKeyServiceTest {

  private static final String BATCH_TAG_ID = "2020-09-01-07-0";

  @Autowired
  private FederationUploadKeyService uploadKeyService;

  @MockBean
  private FederationUploadKeyRepository uploadKeyRepository;

  @MockBean
  private KeySharingPoliciesChecker keySharingPoliciesChecker;


  @Test
  void shouldRetrieveKeysWithConsentOnly() {
    var testKeys = List.of(
        FederationUploadKey.from(buildDiagnosisKeyForSubmissionTimestamp(1000L, true)),
        FederationUploadKey.from(buildDiagnosisKeyForSubmissionTimestamp(2000L, false)));

    when(uploadKeyRepository.findAllUploadableKeys()).thenReturn(testKeys);
    when(keySharingPoliciesChecker.canShareKeyAtTime(any(), any(), any())).thenReturn(true);

    var actKeys = uploadKeyService.getPendingUploadKeys(ExpirationPolicy.of(0, ChronoUnit.MINUTES));
    Assertions.assertThat(actKeys).hasSize(1);
    assertDiagnosisKeysEqual(testKeys.get(0), actKeys.get(0));
  }

  @Test
  void shouldRetrieveExpiredKeysOnly() {
    FederationUploadKey key1 = FederationUploadKey.from(buildDiagnosisKeyForSubmissionTimestamp(1000L, true));
    FederationUploadKey key2 = FederationUploadKey.from(buildDiagnosisKeyForSubmissionTimestamp(2000L, false));
    var testKeys = List.of(key1, key2);

    when(uploadKeyRepository.findAllUploadableKeys()).thenReturn(testKeys);
    when(keySharingPoliciesChecker.canShareKeyAtTime(eq(key1), any(), any())).thenReturn(true);
    when(keySharingPoliciesChecker.canShareKeyAtTime(eq(key2), any(), any())).thenReturn(false);

    var actKeys = uploadKeyService.getPendingUploadKeys(ExpirationPolicy.of(120, ChronoUnit.MINUTES));
    Assertions.assertThat(actKeys).hasSize(1);
    assertDiagnosisKeysEqual(testKeys.get(0), actKeys.get(0));
  }

  @Test
  void shouldUpdateBatchTagId() {
    FederationUploadKey key1 = FederationUploadKey.from(buildDiagnosisKeyForSubmissionTimestamp(1000L, true));
    FederationUploadKey key2 = FederationUploadKey.from(buildDiagnosisKeyForSubmissionTimestamp(2000L, false));
    var testKeys = List.of(key1, key2);

    uploadKeyService.updateBatchTagForKeys(testKeys, BATCH_TAG_ID);
    verify(uploadKeyRepository, times(1)).updateBatchTag(key1.getKeyData(), BATCH_TAG_ID);
    verify(uploadKeyRepository, times(1)).updateBatchTag(key2.getKeyData(), BATCH_TAG_ID);
  }
}
