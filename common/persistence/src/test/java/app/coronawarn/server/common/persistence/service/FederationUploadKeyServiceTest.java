

package app.coronawarn.server.common.persistence.service;

import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.assertDiagnosisKeysEqual;
import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.buildDiagnosisKeyForSubmissionTimestamp;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.common.persistence.service.common.ExpirationPolicy;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@DataJdbcTest
class FederationUploadKeyServiceTest {

  private static final String BATCH_TAG_ID = "2020-09-01-07-0";

  @Autowired
  private FederationUploadKeyService uploadKeyService;

  @MockBean
  private FederationUploadKeyRepository uploadKeyRepository;

  @MockBean
  private KeySharingPoliciesChecker keySharingPoliciesChecker;

  public static int DAYS_TO_RETAIN = 14;

  /**
   * The submission timestamp is counted in 1 hour intervals since epoch.
   */
  public static final long ONE_HOUR_INTERVAL_SECONDS = TimeUnit.HOURS.toSeconds(1);

  /**
   * The rolling start interval number is counted in 10 minute intervals since epoch.
   */
  public static final long TEN_MINUTES_INTERVAL_SECONDS = TimeUnit.MINUTES.toSeconds(10);

  @Test
  void shouldRetrieveKeysWithConsentOnly() {
    var testKeys = List.of(
        FederationUploadKey.from(buildDiagnosisKeyForSubmissionTimestamp(1000L, true)),
        FederationUploadKey.from(buildDiagnosisKeyForSubmissionTimestamp(2000L, false)));

    when(uploadKeyRepository.findAllUploadableKeys()).thenReturn(testKeys);
    when(keySharingPoliciesChecker.canShareKeyAtTime(any(), any(), any())).thenReturn(true);

    var actKeys = uploadKeyService.getPendingUploadKeys(ExpirationPolicy.of(0, ChronoUnit.MINUTES), Integer.MAX_VALUE);
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

    var actKeys = uploadKeyService.getPendingUploadKeys(ExpirationPolicy.of(120, ChronoUnit.MINUTES), Integer.MAX_VALUE);
    Assertions.assertThat(actKeys).hasSize(1);
    assertDiagnosisKeysEqual(testKeys.get(0), actKeys.get(0));
  }

  long toSubmissionTimestamp(LocalDateTime t) {
    return t.toEpochSecond(ZoneOffset.UTC) / ONE_HOUR_INTERVAL_SECONDS;
  }

  int toRollingInterval(LocalDateTime t) {
    return (int)(t.toEpochSecond(ZoneOffset.UTC) / TEN_MINUTES_INTERVAL_SECONDS);
  }


  @Test
  void shouldRetrieveKeysUnderRetentionOnly() {
    var today = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS);

    FederationUploadKey recentKey = FederationUploadKey.from(buildDiagnosisKeyForSubmissionTimestamp(
        toSubmissionTimestamp(today.minusDays(1)), toRollingInterval(today.minusDays(1)), true));
    FederationUploadKey oldKey = FederationUploadKey.from(buildDiagnosisKeyForSubmissionTimestamp(
        toSubmissionTimestamp(today.minusDays(1)), toRollingInterval(today.minusDays(15)), true));
    var testKeys = List.of(recentKey, oldKey);
    when(uploadKeyRepository.findAllUploadableKeys()).thenReturn(testKeys);
    when(keySharingPoliciesChecker.canShareKeyAtTime(any(), any(), any())).thenReturn(true);

    var actKeys = uploadKeyService.getPendingUploadKeys(ExpirationPolicy.of(0, ChronoUnit.MINUTES), DAYS_TO_RETAIN);
    Assertions.assertThat(actKeys)
        .hasSize(1)
        .doesNotContain(oldKey);
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
