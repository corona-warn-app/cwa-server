package app.coronawarn.server.services.federation.upload.integration;

import static app.coronawarn.server.services.federation.upload.utils.MockData.generateRandomUploadKey;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

abstract class DiagnosisKeyReplicationIT extends UploadKeyIT{

  @Autowired
  private DiagnosisKeyService keyService;

  @Autowired
  private DiagnosisKeyRepository keyRepository;

  @Autowired
  private FederationUploadKeyRepository uploadkeyRepository;

  @ActiveProfiles({"disable-ssl-efgs-verification", "connect-efgs"})
  public static class ReplicationForEfgsTest extends DiagnosisKeyReplicationIT {

    @Test
    void diagnosisKeysWithConsentShouldBeReplicatedOnInsert() {
      persistNewKeyAndCheckReplication();
    }

    @Test
    void diagnosisKeysWithoutConsentShouldNotBeReplicatedOnInsert() {
      persistNewKeysAndCheckReplicationWhenConsentIsFalse();
    }

    @Test
    void deletionOfDiagnosisKeysSHouldBeReplicatedToUploadTable() {
      deleteDiagnosisKeysAndVerifyItPropagatedToUploadTables();
    }
  }

  @ActiveProfiles({"disable-ssl-efgs-verification", "connect-sgs"})
  public static class ReplicationForSgsTest extends DiagnosisKeyReplicationIT {

    @Test
    void diagnosisKeysWithConsentShouldBeReplicatedOnInsert() {
      persistNewKeyAndCheckReplication();
    }

    @Test
    void diagnosisKeysWithoutConsentShouldNotBeReplicatedOnInsert() {
      persistNewKeysAndCheckReplicationWhenConsentIsFalse();
    }

    @Test
    void deletionOfDiagnosisKeysSHouldBeReplicatedToUploadTable() {
      deleteDiagnosisKeysAndVerifyItPropagatedToUploadTables();
    }
  }

  protected void persistNewKeysAndCheckReplicationWhenConsentIsFalse() {
    DiagnosisKey dummyKey = generateRandomUploadKey(false);
    keyService.saveDiagnosisKeys(List.of(dummyKey));

    Collection<FederationUploadKey> uploadableKeys = uploadkeyRepository.findAllUploadableKeys();
    assertTrue(uploadableKeys.isEmpty());
  }

  protected void deleteDiagnosisKeysAndVerifyItPropagatedToUploadTables() {
    DiagnosisKey dummyKey = persistNewKeyAndCheckReplication();
    keyRepository.delete(dummyKey);
    Collection<FederationUploadKey> uploadableKeys = uploadkeyRepository.findAllUploadableKeys();
    assertTrue(uploadableKeys.isEmpty());
  }

  protected DiagnosisKey persistNewKeyAndCheckReplication() {
    DiagnosisKey dummyKey = generateRandomUploadKey(true);
    keyService.saveDiagnosisKeys(List.of(dummyKey));

    Collection<FederationUploadKey> uploadableKeys = uploadkeyRepository.findAllUploadableKeys();

    assertEquals(1, uploadableKeys.size());
    assertArrayEquals(dummyKey.getKeyData(), uploadableKeys.iterator().next().getKeyData());
    return dummyKey;
  }
}
