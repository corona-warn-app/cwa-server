
package app.coronawarn.server.services.federation.upload.runner;

import static org.assertj.core.util.Lists.emptyList;
import static org.assertj.core.util.Lists.list;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.federation.client.upload.BatchUploadResponse;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.common.persistence.service.FederationUploadKeyService;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import app.coronawarn.server.common.persistence.service.common.ValidDiagnosisKeyFilter;
import app.coronawarn.server.services.federation.upload.client.FederationUploadClient;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import app.coronawarn.server.services.federation.upload.keys.DiagnosisKeyLoader;
import app.coronawarn.server.services.federation.upload.payload.AllowedPropertiesMap;
import app.coronawarn.server.services.federation.upload.payload.DiagnosisKeyBatchAssembler;
import app.coronawarn.server.services.federation.upload.payload.PayloadFactory;
import app.coronawarn.server.services.federation.upload.payload.signing.BatchSigner;
import app.coronawarn.server.services.federation.upload.payload.signing.CryptoProvider;
import app.coronawarn.server.services.federation.upload.utils.MockData;
import com.google.protobuf.ByteString;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = UploadServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Upload.class, PayloadFactory.class, DiagnosisKeyBatchAssembler.class,
    BatchSigner.class, CryptoProvider.class, FederationUploadKeyService.class, ValidDiagnosisKeyFilter.class,
    KeySharingPoliciesChecker.class, AllowedPropertiesMap.class},
    initializers = ConfigFileApplicationContextInitializer.class)
class UploadResponseTest {

  @MockBean
  FederationUploadClient mockUploadClient;

  @MockBean
  FederationUploadKeyRepository mockUploadKeyRepository;

  @MockBean
  DiagnosisKeyLoader mockDiagnosisKeyLoader;

  @SpyBean
  UploadServiceConfig uploadServiceConfig;

  @Autowired
  Upload upload;

  @BeforeEach
  void setup() {
    when(uploadServiceConfig.getMinBatchKeyCount()).thenReturn(2);
  }

  private void returnFromUpload(BatchUploadResponse response) {
    when(mockUploadClient.postBatchUpload(any())).thenReturn(Optional.of(response));
  }

  private void returnEmptyFromUpload() {
    when(mockUploadClient.postBatchUpload(any())).thenReturn(Optional.empty());
  }

  @Test
  void check201UploadResponseStatus() throws Exception {
    var testKey1 = MockData.generateRandomUploadKey(true);
    var testKey2 = MockData.generateRandomUploadKey(true);

    when(mockDiagnosisKeyLoader.loadDiagnosisKeys()).thenReturn(List.of(testKey1, testKey2));
    returnEmptyFromUpload();
    upload.run(null);
    verify(mockUploadKeyRepository, times(1))
        .updateBatchTag(eq(testKey1.getKeyData()), any());
    verify(mockUploadKeyRepository, times(1))
        .updateBatchTag(eq(testKey2.getKeyData()), any());
  }

  @Test
  void check409UploadResponseStatus() throws Exception {
    var testKey1 = MockData.generateRandomUploadKey(true);
    var testKey2 = MockData.generateRandomUploadKey(true);

    when(mockDiagnosisKeyLoader.loadDiagnosisKeys()).thenReturn(List.of(testKey1, testKey2));
    returnFromUpload(createFake409Response());
    upload.run(null);
    verify(mockUploadKeyRepository, times(1))
        .updateBatchTag(eq(testKey1.getKeyData()), any());
    verify(mockUploadKeyRepository, times(1))
        .updateBatchTag(eq(testKey2.getKeyData()), any());
  }

  @Test
  void check500UploadResponseStatus() throws Exception {
    var testKey1 = MockData.generateRandomUploadKey(true);
    var testKey2 = MockData.generateRandomUploadKey(true);

    when(uploadServiceConfig.getMinBatchKeyCount()).thenReturn(2);
    when(mockDiagnosisKeyLoader.loadDiagnosisKeys()).thenReturn(List.of(testKey1, testKey2));
    returnFromUpload(createFake500Response());
    upload.run(null);
    verify(mockUploadKeyRepository, never())
        .updateBatchTag(eq(testKey1.getKeyData()), any());
    verify(mockUploadKeyRepository, never())
        .updateBatchTag(eq(testKey2.getKeyData()), any());
  }

  @Test
  void check201And409UploadResponseStatus() throws Exception {
    var testKey1 = MockData.generateRandomUploadKey(true);
    var testKey2 = MockData.generateRandomUploadKey(true);

    when(uploadServiceConfig.getMinBatchKeyCount()).thenReturn(2);
    when(mockDiagnosisKeyLoader.loadDiagnosisKeys()).thenReturn(List.of(testKey1, testKey2));
    returnFromUpload(createFake409And201Response());
    upload.run(null);
    verify(mockUploadKeyRepository, times(1))
        .updateBatchTag(eq(testKey1.getKeyData()), any());
    verify(mockUploadKeyRepository, times(1))
        .updateBatchTag(eq(testKey2.getKeyData()), any());
  }

  @Test
  void check201And500UploadResponseStatus() throws Exception {
    List<FederationUploadKey> orderedKeys = list(MockData.generateRandomUploadKey(true),
        MockData.generateRandomUploadKey(true)).stream()
        .sorted(Comparator.comparing(diagnosisKey ->
            ByteString.copyFrom(diagnosisKey.getKeyData()).toStringUtf8())).collect(Collectors.toList());
    when(uploadServiceConfig.getMinBatchKeyCount()).thenReturn(2);
    when(mockDiagnosisKeyLoader.loadDiagnosisKeys()).thenReturn(orderedKeys);
    returnFromUpload(createFake500And201Response());
    upload.run(null);
    verify(mockUploadKeyRepository, never())
        .updateBatchTag(eq(orderedKeys.get(0).getKeyData()), any());
      verify(mockUploadKeyRepository, times(1))
          .updateBatchTag(eq(orderedKeys.get(1).getKeyData()), any());
  }

  @Test
  void check409And500UploadResponseStatus() throws Exception {
    List<FederationUploadKey> orderedKeys = list(MockData.generateRandomUploadKey(true),
        MockData.generateRandomUploadKey(true)).stream()
        .sorted(Comparator.comparing(diagnosisKey ->
            ByteString.copyFrom(diagnosisKey.getKeyData()).toStringUtf8())).collect(Collectors.toList());

    when(uploadServiceConfig.getMinBatchKeyCount()).thenReturn(2);
    when(mockDiagnosisKeyLoader.loadDiagnosisKeys()).thenReturn(orderedKeys);
    returnFromUpload(createFake409And500Response());
    upload.run(null);
    verify(mockUploadKeyRepository, times(1))
        .updateBatchTag(eq(orderedKeys.get(0).getKeyData()), any());
    verify(mockUploadKeyRepository, never())
        .updateBatchTag(eq(orderedKeys.get(1).getKeyData()), any());
  }

  private BatchUploadResponse createFake409And500Response() {
    return new BatchUploadResponse(list("0"), list("1"), emptyList());
  }

  private BatchUploadResponse createFake500And201Response() {
    return new BatchUploadResponse(emptyList(), list("0"), list("1"));
  }

  private BatchUploadResponse createFake409And201Response() {
    return new BatchUploadResponse(list("0"), emptyList(), list("1"));
  }

  private BatchUploadResponse createFake409Response() {
    return new BatchUploadResponse(list("0", "1"), emptyList(), emptyList());
  }

  private BatchUploadResponse createFake500Response() {
    return new BatchUploadResponse(emptyList(), list("0", "1"), emptyList());
  }
}
