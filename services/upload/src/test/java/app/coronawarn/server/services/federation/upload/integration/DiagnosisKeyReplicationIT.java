package app.coronawarn.server.services.federation.upload.integration;

import static app.coronawarn.server.services.federation.upload.utils.MockData.generateRandomUploadKey;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

abstract class DiagnosisKeyReplicationIT extends UploadKeyIT {

  @Autowired
  private DiagnosisKeyService keyService;

  @Autowired
  private DiagnosisKeyRepository keyRepository;

  @Autowired
  private FederationUploadKeyRepository uploadkeyRepository;

  public static final SecureRandom random = new SecureRandom();

  @ActiveProfiles({"disable-ssl-efgs-verification", "connect-efgs"})
  public static class ReplicationForEfgsTest extends DiagnosisKeyReplicationIT {

    @Test
    void diagnosisKeysWithConsentAndPcrTestShouldBeReplicatedOnInsert() {
      persistNewKeyAndCheckReplicationWhenConsentIsTrueAndSubmissionTypeIsPcr();
    }

    @Test
    void diagnosisKeysWithoutConsentShouldNotBeReplicatedOnInsert() {
      persistNewKeysAndCheckReplicationWhenConsentIsFalse();
    }

    @Test
    void diagnosisKeysWithoutSubmissionTypePcrShouldNotBeReplicatedOnInsert() {
      persistNewKeysAndCheckReplicationWhenSubmissionTypeIsNotPcr();
    }

    @Test
    void deletionOfDiagnosisKeysShouldBeReplicatedToUploadTableWhenSubmissionTypeIsPcr() {
      deletePcrDiagnosisKeysAndVerifyItPropagatedToUploadTables();
    }

    @Test
    void deletionOfDiagnosisKeysShouldNotBeReplicatedToUploadTableWhenSubmissionTypeIsNotPcr() {
      deleteNonPcrDiagnosisKeysAndVerifyItIsNotPropagatedToUploadTables();
    }
  }

  @ActiveProfiles({"disable-ssl-efgs-verification", "connect-chgs"})
  public static class ReplicationForSgsTest extends DiagnosisKeyReplicationIT {

    @Test
    void diagnosisKeysWithConsentAndPcrTestShouldBeReplicatedOnInsert() {
      persistNewKeyAndCheckReplicationWhenConsentIsTrueAndSubmissionTypeIsPcr();
    }

    @Test
    void diagnosisKeysWithoutConsentShouldNotBeReplicatedOnInsert() {
      persistNewKeysAndCheckReplicationWhenConsentIsFalse();
    }

    @Test
    void diagnosisKeysWithoutSubmissionTypePcrShouldNotBeReplicatedOnInsert() {
      persistNewKeysAndCheckReplicationWhenSubmissionTypeIsNotPcr();
    }

    @Test
    void deletionOfDiagnosisKeysShouldBeReplicatedToUploadTable() {
      deletePcrDiagnosisKeysAndVerifyItPropagatedToUploadTables();
    }

    @Test
    void deletionOfDiagnosisKeysShouldNotBeReplicatedToUploadTableWhenSubmissionTypeIsNotPcr() {
      deleteNonPcrDiagnosisKeysAndVerifyItIsNotPropagatedToUploadTables();
    }
  }

  protected void persistNewKeysAndCheckReplicationWhenConsentIsFalse() {
    keyService.saveDiagnosisKeys(List.of(
        generateRandomUploadKey(false, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        generateRandomUploadKey(false, SubmissionType.SUBMISSION_TYPE_RAPID_TEST)
    ));

    Collection<FederationUploadKey> uploadableKeys = uploadkeyRepository.findAllUploadableKeys();
    assertTrue(uploadableKeys.isEmpty());
  }

  protected void deletePcrDiagnosisKeysAndVerifyItPropagatedToUploadTables() {
    DiagnosisKey dummyKey = persistNewKeyAndCheckReplicationWhenConsentIsTrueAndSubmissionTypeIsPcr();
    keyRepository.delete(dummyKey);
    Collection<FederationUploadKey> uploadableKeys = uploadkeyRepository.findAllUploadableKeys();
    assertTrue(uploadableKeys.isEmpty());
  }

  protected void deleteNonPcrDiagnosisKeysAndVerifyItIsNotPropagatedToUploadTables() {
    DiagnosisKey rapidKey = generateRandomUploadKey(true, SubmissionType.SUBMISSION_TYPE_RAPID_TEST);
    DiagnosisKey pcrKey = generateRandomUploadKey(true, SubmissionType.SUBMISSION_TYPE_PCR_TEST);
    keyService.saveDiagnosisKeys(List.of(rapidKey, pcrKey));

    keyRepository.delete(rapidKey);
    Collection<FederationUploadKey> uploadableKeys = uploadkeyRepository.findAllUploadableKeys();
    assertTrue(uploadableKeys.contains(pcrKey));
    assertFalse(uploadableKeys.contains(rapidKey));
  }

  protected DiagnosisKey persistNewKeyAndCheckReplicationWhenConsentIsTrueAndSubmissionTypeIsPcr() {
    DiagnosisKey dummyKey = generateRandomUploadKey(true, SubmissionType.SUBMISSION_TYPE_PCR_TEST);
    keyService.saveDiagnosisKeys(List.of(dummyKey));

    Collection<FederationUploadKey> uploadableKeys = uploadkeyRepository.findAllUploadableKeys();

    assertEquals(1, uploadableKeys.size());
    assertArrayEquals(dummyKey.getKeyData(), uploadableKeys.iterator().next().getKeyData());
    return dummyKey;
  }

  protected void persistNewKeysAndCheckReplicationWhenSubmissionTypeIsNotPcr() {
    keyService.saveDiagnosisKeys(List.of(
        generateRandomUploadKey(true, SubmissionType.SUBMISSION_TYPE_RAPID_TEST)
    ));

    Collection<FederationUploadKey> uploadableKeys = uploadkeyRepository.findAllUploadableKeys();
    assertTrue(uploadableKeys.isEmpty());
  }
}
