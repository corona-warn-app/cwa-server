package app.coronawarn.server.services.federation.upload.integration;

import static app.coronawarn.server.services.federation.upload.utils.MockData.generateRandomUploadKey;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.common.persistence.repository.SgsUploadKeyRepository;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("disable-ssl-efgs-verification")
class DiagnosisKeyReplicationIT extends UploadKeyIT {

  @Autowired
  private DiagnosisKeyService keyService;

  @Autowired
  private DiagnosisKeyRepository keyRepository;

  @Autowired
  private FederationUploadKeyRepository uploadKeyRepository;

  @Autowired
  private SgsUploadKeyRepository sgsKeyRepository;


  @Test
  void diagnosisKeysWithConsentShouldBeReplicatedOnInsert() {
    persistNewKeyAndCheckReplication();
  }

  @Test
  void diagnosisKeysWithoutConsentShouldNotBeReplicatedOnInsert() {
    DiagnosisKey dummyKey = generateRandomUploadKey(false);
    keyService.saveDiagnosisKeys(List.of(dummyKey));

    Collection<FederationUploadKey> uploadableKeys = uploadKeyRepository.findAllUploadableKeys();
    Collection<FederationUploadKey> uploadableSwissKeys = sgsKeyRepository.findAllUploadableKeys();

    assertTrue(uploadableKeys.isEmpty());
    assertTrue(uploadableSwissKeys.isEmpty());
  }

  @Test
  void deletionOfDiagnosisKeysSHouldBeReplicatedToUploadTable() {
    DiagnosisKey dummyKey = persistNewKeyAndCheckReplication();
    keyRepository.delete(dummyKey);
    Collection<FederationUploadKey> uploadableKeys = uploadKeyRepository.findAllUploadableKeys();
    Collection<FederationUploadKey> uploadableSwissKeys = sgsKeyRepository.findAllUploadableKeys();
    assertTrue(uploadableKeys.isEmpty());
    assertTrue(uploadableSwissKeys.isEmpty());
  }

  private DiagnosisKey persistNewKeyAndCheckReplication() {
    DiagnosisKey dummyKey = generateRandomUploadKey(true);
    keyService.saveDiagnosisKeys(List.of(dummyKey));

    Collection<FederationUploadKey> uploadableKeys = uploadKeyRepository.findAllUploadableKeys();
    Collection<FederationUploadKey> uploadableSwissKeys = sgsKeyRepository.findAllUploadableKeys();

    assertEquals(1, uploadableKeys.size());
    assertEquals(1, uploadableSwissKeys.size());
    assertArrayEquals(dummyKey.getKeyData(), uploadableKeys.iterator().next().getKeyData());
    assertArrayEquals(dummyKey.getKeyData(), uploadableSwissKeys.iterator().next().getKeyData());
    return dummyKey;
  }
}
