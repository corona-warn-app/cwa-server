package app.coronawarn.server.services.federation.upload.integration;

import static app.coronawarn.server.services.federation.upload.utils.MockData.generateRandomDiagnosisKeys;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.federation.client.upload.BatchUploadResponse;
import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.federation.upload.client.FederationUploadClient;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import app.coronawarn.server.services.federation.upload.runner.Upload;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

abstract class DiagnosisKeyUploadIT extends UploadKeyIT {

  @ActiveProfiles({ "disable-ssl-efgs-verification", "connect-efgs" })
  public static class UploadKeyEfgsIT extends DiagnosisKeyUploadIT {
    @Test
    void shouldUpdateBatchTagIdsForSuccesfullyUploadedKeys() throws Exception {
      uploadKeysAndTestBatchTagIdUpdate();
    }
  }

  @ActiveProfiles({ "disable-ssl-efgs-verification", "connect-chgs" })
  public static class UploadKeySgsIT extends DiagnosisKeyUploadIT {
    @Test
    void shouldNotUpdateBatchTagIdsForUploadedKeysWith409() throws Exception {
      final BatchUploadResponse batchUploadResponse = mock(BatchUploadResponse.class);
      when(batchUploadResponse.getStatus409()).thenReturn(indices(0, 4000));
      when(federationUploadClient.postBatchUpload(any())).thenReturn(Optional.of(batchUploadResponse));
      final Iterable<FederationUploadKey> currentKeys = uploadKeysAndTestBatchTagIdUpdate();
      currentKeys.forEach(DiagnosisKeyUploadIT::assertBatchTagIsNull);
    }

    @Test
    void shouldNotUpdateBatchTagIdsForUploadedKeysWith500() throws Exception {
      final BatchUploadResponse batchUploadResponse = mock(BatchUploadResponse.class);
      when(batchUploadResponse.getStatus500()).thenReturn(indices(0, 4000));
      when(federationUploadClient.postBatchUpload(any())).thenReturn(Optional.of(batchUploadResponse));
      final Iterable<FederationUploadKey> currentKeys = uploadKeysAndTestBatchTagIdUpdate();
      currentKeys.forEach(DiagnosisKeyUploadIT::assertBatchTagIsNull);
    }

    @Test
    void shouldNotUpdateBatchTagIdsForUploadedKeysWithMixture201409() throws Exception {
      final BatchUploadResponse batchUploadResponse = mock(BatchUploadResponse.class);
      when(batchUploadResponse.getStatus201()).thenReturn(indices(0, 2000));
      when(batchUploadResponse.getStatus409()).thenReturn(indices(2000, 4000));
      when(federationUploadClient.postBatchUpload(any())).thenReturn(Optional.of(batchUploadResponse));
      final Iterable<FederationUploadKey> currentKeys = uploadKeysAndTestBatchTagIdUpdate();
      currentKeys.forEach(DiagnosisKeyUploadIT::assertBatchTagIsNull);
    }

    @Test
    void shouldNotUpdateBatchTagIdsForUploadedKeysWithMixture201500() throws Exception {
      final BatchUploadResponse batchUploadResponse = mock(BatchUploadResponse.class);
      when(batchUploadResponse.getStatus201()).thenReturn(indices(0, 2000));
      when(batchUploadResponse.getStatus500()).thenReturn(indices(2000, 4000));
      when(federationUploadClient.postBatchUpload(any())).thenReturn(Optional.of(batchUploadResponse));
      final Iterable<FederationUploadKey> currentKeys = uploadKeysAndTestBatchTagIdUpdate();
      currentKeys.forEach(DiagnosisKeyUploadIT::assertBatchTagIsNull);
    }

    @Test
    void shouldNotUpdateBatchTagIdsForUploadedKeysWithMixtureAll() throws Exception {
      final BatchUploadResponse batchUploadResponse = mock(BatchUploadResponse.class);
      final List<String> success = indices(0, 1000);
      success.addAll(indices(3000, 4000));
      when(batchUploadResponse.getStatus201()).thenReturn(success);
      when(batchUploadResponse.getStatus409()).thenReturn(indices(1000, 2000));
      when(batchUploadResponse.getStatus500()).thenReturn(indices(2000, 3000));
      when(federationUploadClient.postBatchUpload(any())).thenReturn(Optional.of(batchUploadResponse));
      final Iterable<FederationUploadKey> currentKeys = uploadKeysAndTestBatchTagIdUpdate();
      currentKeys.forEach(DiagnosisKeyUploadIT::assertBatchTagIsNull);
    }

    @Test
    void shouldUpdateBatchTagIdsForSuccessfullyUploadedKeys() throws Exception {
      final BatchUploadResponse batchUploadResponse = mock(BatchUploadResponse.class);
      when(batchUploadResponse.getStatus201()).thenReturn(indices(0, 4000));
      when(federationUploadClient.postBatchUpload(any())).thenReturn(Optional.of(batchUploadResponse));
      final Iterable<FederationUploadKey> currentKeys = uploadKeysAndTestBatchTagIdUpdate();
      currentKeys.forEach(DiagnosisKeyUploadIT::assertKeyWasMarkedWithBatchTag);
    }
  }

  @Autowired
  private DiagnosisKeyService keyService;

  @Autowired
  private FederationUploadKeyRepository uploadKeyRepository;

  @Autowired
  private Upload runner;

  @Autowired
  private UploadServiceConfig uploadConfig;

  @MockBean
  FederationUploadClient federationUploadClient;

  public static void assertKeyWasMarkedWithBatchTag(final FederationUploadKey uploadKey) {
    assertNotNull(uploadKey.getBatchTag());
  }

  public static void assertBatchTagIsNull(final FederationUploadKey uploadKey) {
    assertNull(uploadKey.getBatchTag());
  }

  List<String> indices(final int start, final int end) {
    return IntStream.range(start, end).boxed().map(String::valueOf).collect(Collectors.toList());
  }

  protected Iterable<FederationUploadKey> uploadKeysAndTestBatchTagIdUpdate() throws Exception {
    final List<DiagnosisKey> dummyKeys = generateRandomDiagnosisKeys(true, uploadConfig.getMaxBatchKeyCount() * 2);
    keyService.saveDiagnosisKeys(dummyKeys); // replicated to upload table
    runner.run(null);
    return uploadKeyRepository.findAll();
  }
}
