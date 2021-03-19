

package app.coronawarn.server.services.federation.upload.integration;

import static app.coronawarn.server.services.federation.upload.utils.MockData.generateRandomDiagnosisKeys;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.federation.upload.client.FederationUploadClient;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import app.coronawarn.server.services.federation.upload.runner.Upload;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

abstract class DiagnosisKeyUploadIT extends UploadKeyIT {

  @Autowired
  private DiagnosisKeyService keyService;

  @Autowired
  private FederationUploadKeyRepository uploadKeyRepository;

  @Autowired
  private Upload runner;

  @Autowired
  private UploadServiceConfig uploadConfig;

  @MockBean
  private FederationUploadClient federationUploadClient;

  @ActiveProfiles({"disable-ssl-efgs-verification", "connect-chgs"})
  public static class UploadKeySgsIT extends DiagnosisKeyUploadIT {

    @Test
    void shouldUpdateBatchTagIdsForSuccessfullyUploadedKeys() throws Exception {
      uploadKeysAndTestBatchTagIdUpdate();
    }
  }

  @ActiveProfiles({"disable-ssl-efgs-verification", "connect-efgs"})
  public static class UploadKeyEfgsIT extends DiagnosisKeyUploadIT {

    @Test
    void shouldUpdateBatchTagIdsForSuccesfullyUploadedKeys() throws Exception {
      uploadKeysAndTestBatchTagIdUpdate();
    }
  }

  protected void uploadKeysAndTestBatchTagIdUpdate() throws Exception {
    List<DiagnosisKey> dummyKeys = generateRandomDiagnosisKeys(true, uploadConfig.getMaxBatchKeyCount() * 2);
    keyService.saveDiagnosisKeys(dummyKeys); //replicated to upload table

    runner.run(null);

    Iterable<FederationUploadKey> currentKeys = uploadKeyRepository.findAll();

    currentKeys.forEach(this::assertKeyWasMarkedWithBatchTag);
  }

  private void assertKeyWasMarkedWithBatchTag(FederationUploadKey uploadKey) {
    Assertions.assertNotNull(uploadKey.getBatchTag());
  }
}
