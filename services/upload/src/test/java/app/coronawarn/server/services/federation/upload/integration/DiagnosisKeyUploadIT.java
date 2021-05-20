package app.coronawarn.server.services.federation.upload.integration;

import static app.coronawarn.server.services.federation.upload.utils.MockData.generateRandomDiagnosisKeys;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

abstract class DiagnosisKeyUploadIT extends UploadKeyIT {

  public static final int TEST_KEYS_COUNT = 8000;

  public static void assertBatchTagIsNull(final FederationUploadKey uploadKey) {
    assertNull(uploadKey.getBatchTag());
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

  List<String> indices(final int start, final int end) {
    return IntStream.range(start, end).boxed().map(String::valueOf).collect(Collectors.toList());
  }

  @Test
  void shouldNotUpdateBatchTagIdsForUploadedKeysWith409() throws Exception {
    final BatchUploadResponse batchUploadResponse = mock(BatchUploadResponse.class);
    when(batchUploadResponse.getStatus409()).thenReturn(indices(0, 4000));
    when(federationUploadClient.postBatchUpload(any())).thenReturn(Optional.of(batchUploadResponse));
    uploadKeysAndTestBatchTagIdUpdate();
    assertEquals(0, uploadKeyRepository.findAllUploadableKeys().size());
  }

  @Test
  void shouldNotUpdateBatchTagIdsForUploadedKeysWith500() throws Exception {
    final BatchUploadResponse batchUploadResponse = mock(BatchUploadResponse.class);
    when(batchUploadResponse.getStatus500()).thenReturn(indices(0, 4000));
    when(federationUploadClient.postBatchUpload(any())).thenReturn(Optional.of(batchUploadResponse));
    final Iterable<FederationUploadKey> currentKeys = uploadKeysAndTestBatchTagIdUpdate();
    assertEquals(TEST_KEYS_COUNT, uploadKeyRepository.findAllUploadableKeys().size());
    currentKeys.forEach(DiagnosisKeyUploadIT::assertBatchTagIsNull);
  }

  @Test
  void shouldNotUpdateBatchTagIdsForUploadedKeysWithMixture201409() throws Exception {
    final BatchUploadResponse batchUploadResponse = mock(BatchUploadResponse.class);
    when(batchUploadResponse.getStatus201()).thenReturn(indices(0, 2000));
    when(batchUploadResponse.getStatus409()).thenReturn(indices(2000, 4000));
    when(federationUploadClient.postBatchUpload(any())).thenReturn(Optional.of(batchUploadResponse));
    uploadKeysAndTestBatchTagIdUpdate();
    assertEquals(TEST_KEYS_COUNT / 2, uploadKeyRepository.findAllUploadableKeys().size());
  }

  @Test
  void shouldNotUpdateBatchTagIdsForUploadedKeysWithMixture201500() throws Exception {
    final BatchUploadResponse batchUploadResponse = mock(BatchUploadResponse.class);
    when(batchUploadResponse.getStatus201()).thenReturn(indices(0, 2000));
    when(batchUploadResponse.getStatus500()).thenReturn(indices(2000, 4000));
    when(federationUploadClient.postBatchUpload(any())).thenReturn(Optional.of(batchUploadResponse));
    final Iterable<FederationUploadKey> currentKeys = uploadKeysAndTestBatchTagIdUpdate();
    assertEquals(TEST_KEYS_COUNT, uploadKeyRepository.findAllUploadableKeys().size());
    currentKeys.forEach(DiagnosisKeyUploadIT::assertBatchTagIsNull);
  }

  @Test
  void shouldNotUpdateBatchTagIdsForUploadedKeysWithMixtureAll() throws Exception {
    final BatchUploadResponse batchUploadResponse = mock(BatchUploadResponse.class);
    final List<String> success = indices(0, 1000);
    success.addAll(indices(3000, 4000));
    when(batchUploadResponse.getStatus201()).thenReturn(success);
    when(batchUploadResponse.getStatus409()).thenReturn(indices(1000, 2000)); // 2 batches == 2000 keys conflict
    when(batchUploadResponse.getStatus500()).thenReturn(indices(2000, 3000));
    when(federationUploadClient.postBatchUpload(any())).thenReturn(Optional.of(batchUploadResponse));
    uploadKeysAndTestBatchTagIdUpdate();
    assertEquals(TEST_KEYS_COUNT - 2000, uploadKeyRepository.findAllUploadableKeys().size());
  }

  @Test
  void shouldUpdateBatchTagIdsForSuccessfullyUploadedKeys() throws Exception {
    when(federationUploadClient.postBatchUpload(any()))
        .thenReturn(Optional.ofNullable(null)); // no response body, means success
    uploadKeysAndTestBatchTagIdUpdate();
    assertEquals(0, uploadKeyRepository.findAllUploadableKeys().size());
  }

  @Test
  void testUpdatedBatchTagIdsForSuccessfullyUploadedKeys() throws Exception {
    final BatchUploadResponse batchUploadResponse = mock(BatchUploadResponse.class);
    when(batchUploadResponse.getStatus201()).thenReturn(indices(0, 4000));
    when(federationUploadClient.postBatchUpload(any())).thenReturn(Optional.of(batchUploadResponse));
    final Iterable<FederationUploadKey> currentKeys = uploadKeysAndTestBatchTagIdUpdate();
    currentKeys.forEach(DiagnosisKeyUploadIT::assertKeyWasMarkedWithBatchTag);
  }

  private static void assertKeyWasMarkedWithBatchTag(final FederationUploadKey uploadKey) {
    assertNotNull(uploadKey.getBatchTag());
  }

  private Iterable<FederationUploadKey> uploadKeysAndTestBatchTagIdUpdate() throws Exception {
    final List<DiagnosisKey> dummyKeys = generateRandomDiagnosisKeys(true, uploadConfig.getMaxBatchKeyCount() * 2);
    keyService.saveDiagnosisKeys(dummyKeys); // replicated to upload table
    runner.run(null);
    return uploadKeyRepository.findAll();
  }
}
