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
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

abstract class DiagnosisKeyReplicationIT extends UploadKeyIT {

  @Autowired
  private DiagnosisKeyService keyService;

  @Autowired
  private DiagnosisKeyRepository keyRepository;

  @Autowired
  private FederationUploadKeyRepository uploadkeyRepository;

  private void deleteNonPcrDiagnosisKeysAndVerifyItIsNotPropagatedToUploadTables() {
    final DiagnosisKey rapidKey = generateRandomUploadKey(true, SubmissionType.SUBMISSION_TYPE_RAPID_TEST);
    final DiagnosisKey pcrKey = generateRandomUploadKey(true, SubmissionType.SUBMISSION_TYPE_PCR_TEST);
    keyService.saveDiagnosisKeys(List.of(rapidKey, pcrKey));

    keyRepository.delete(rapidKey);
    final Collection<FederationUploadKey> uploadableKeys = uploadkeyRepository.findAllUploadableKeys();
    assertTrue(uploadableKeys.contains(pcrKey));
    assertFalse(uploadableKeys.contains(rapidKey));
  }

  private void deletePcrDiagnosisKeysAndVerifyItPropagatedToUploadTables() {
    final DiagnosisKey dummyKey = persistNewKeyAndCheckReplicationWhenConsentIsTrueAndSubmissionTypeIsPcr();
    keyRepository.delete(dummyKey);
    final Collection<FederationUploadKey> uploadableKeys = uploadkeyRepository.findAllUploadableKeys();
    assertTrue(uploadableKeys.isEmpty());
  }

  @Test
  void deletionOfDiagnosisKeysShouldBeReplicatedToUploadTable() {
    deletePcrDiagnosisKeysAndVerifyItPropagatedToUploadTables();
  }

  @Test
  void deletionOfDiagnosisKeysShouldBeReplicatedToUploadTableWhenSubmissionTypeIsPcr() {
    deletePcrDiagnosisKeysAndVerifyItPropagatedToUploadTables();
  }

  @Test
  void deletionOfDiagnosisKeysShouldNotBeReplicatedToUploadTableWhenSubmissionTypeIsNotPcr() {
    deleteNonPcrDiagnosisKeysAndVerifyItIsNotPropagatedToUploadTables();
  }

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

  private DiagnosisKey persistNewKeyAndCheckReplicationWhenConsentIsTrueAndSubmissionTypeIsPcr() {
    final DiagnosisKey dummyKey = generateRandomUploadKey(true, SubmissionType.SUBMISSION_TYPE_PCR_TEST);
    keyService.saveDiagnosisKeys(List.of(dummyKey));

    final Collection<FederationUploadKey> uploadableKeys = uploadkeyRepository.findAllUploadableKeys();

    assertEquals(1, uploadableKeys.size());
    assertArrayEquals(dummyKey.getKeyData(), uploadableKeys.iterator().next().getKeyData());
    return dummyKey;
  }

  private void persistNewKeysAndCheckReplicationWhenConsentIsFalse() {
    keyService.saveDiagnosisKeys(List.of(generateRandomUploadKey(false, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        generateRandomUploadKey(false, SubmissionType.SUBMISSION_TYPE_RAPID_TEST)));

    final Collection<FederationUploadKey> uploadableKeys = uploadkeyRepository.findAllUploadableKeys();
    assertTrue(uploadableKeys.isEmpty());
  }

  private void persistNewKeysAndCheckReplicationWhenSubmissionTypeIsNotPcr() {
    keyService.saveDiagnosisKeys(List.of(generateRandomUploadKey(true, SubmissionType.SUBMISSION_TYPE_RAPID_TEST)));

    final Collection<FederationUploadKey> uploadableKeys = uploadkeyRepository.findAllUploadableKeys();
    assertTrue(uploadableKeys.isEmpty());
  }
}
