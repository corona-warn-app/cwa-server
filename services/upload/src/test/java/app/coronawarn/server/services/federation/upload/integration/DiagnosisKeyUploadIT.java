

package app.coronawarn.server.services.federation.upload.integration;

import static app.coronawarn.server.services.federation.upload.utils.UploadKeysMockData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.*;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.federation.upload.client.FederationUploadClient;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import app.coronawarn.server.services.federation.upload.keys.DiagnosisKeyLoader;
import app.coronawarn.server.services.federation.upload.runner.Upload;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

class DiagnosisKeyUploadIT extends UploadKeyIT {

  @Autowired
  private DiagnosisKeyService keyService;

  @Autowired
  private FederationUploadKeyRepository uploadKeyRepository;

  @Autowired
  private Upload runner;

  @Autowired
  private UploadServiceConfig uploadConfig;

  @Autowired
  private DiagnosisKeyLoader uploadKeysLoader;

  @MockBean
  private FederationUploadClient federationUploadClient;

  @Test
  void shouldUpdateBatchTagIdsForSuccesfullyUploadedKeys() throws Exception {
    List<DiagnosisKey> dummyKeys = generateRandomDiagnosisKeys(true, uploadConfig.getMaxBatchKeyCount() * 2);
    keyService.saveDiagnosisKeys(dummyKeys); //replicated to upload table

    runner.run(null);

    Iterable<FederationUploadKey> currentKeys = uploadKeyRepository.findAll();

    currentKeys.forEach(this::assertKeyWasMarkedWithBatchTag);
  }

  @Test
  void shouldAdaptKeysBeforeUploading() throws Exception {
    List<DiagnosisKey> dummyKeys = generateRandomDiagnosisKeys(true, 10);
    keyService.saveDiagnosisKeys(dummyKeys); //replicated to upload table

    List<FederationUploadKey> uploadableKeys = uploadKeysLoader.loadDiagnosisKeys();

    assertEquals(dummyKeys.size(), uploadableKeys.size());
    assertKeyDataIsEqualBetween(dummyKeys, uploadableKeys);

    assertKeysWereAdaptedToEfgsRequirements(dummyKeys, uploadableKeys);
  }

  private void assertKeysWereAdaptedToEfgsRequirements(List<DiagnosisKey> dummyKeys,
      List<FederationUploadKey> uploadableKeys) {
    assertThatOriginCountryHasBeenRemovedFromVisited(dummyKeys, uploadableKeys);
  }

  private void assertThatOriginCountryHasBeenRemovedFromVisited(List<DiagnosisKey> dummyKeys,
      List<FederationUploadKey> uploadableKeys) {
    //first assert we are testing a set of keys which have origin country as part of the visited countries
    dummyKeys.stream().forEach( dummyKey -> {
      assertThat(dummyKey.getVisitedCountries()).contains(dummyKey.getOriginCountry());
    });
    //secondly assert the uploadable keys do not contain the origin country in the visited countries list
    uploadableKeys.stream().forEach( dummyKey -> {
      assertThat(dummyKey.getVisitedCountries()).doesNotContain(dummyKey.getOriginCountry());
    });
  }

  private void assertKeyDataIsEqualBetween(List<DiagnosisKey> dummyKeys,
      List<FederationUploadKey> uploadableKeys) {
    List<byte[]> persistedKeyData = dummyKeys.stream().map(DiagnosisKey::getKeyData).collect(Collectors.toList());
    List<byte[]> uploadableKeyData = uploadableKeys.stream().map(DiagnosisKey::getKeyData).collect(Collectors.toList());
    assertThat(uploadableKeyData).containsAll(persistedKeyData);
  }

  private void assertKeyWasMarkedWithBatchTag(FederationUploadKey uploadKey) {
    Assertions.assertNotNull(uploadKey.getBatchTag());
  }
}
