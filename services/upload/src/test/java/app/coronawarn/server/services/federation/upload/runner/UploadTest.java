/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.federation.upload.runner;

import static app.coronawarn.server.services.federation.upload.utils.MockData.generateRandomDiagnosisKeys;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.FederationUploadKeyService;
import app.coronawarn.server.common.persistence.service.common.ValidDiagnosisKeyFilter;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.federation.upload.client.ProdFederationUploadClient;
import app.coronawarn.server.services.federation.upload.client.TestFederationUploadClient;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import app.coronawarn.server.services.federation.upload.keys.DiagnosisKeyGenerator;
import app.coronawarn.server.services.federation.upload.keys.DiagnosisKeyPersistenceLoader;
import app.coronawarn.server.services.federation.upload.payload.DiagnosisKeyBatchAssembler;
import app.coronawarn.server.services.federation.upload.payload.PayloadFactory;
import app.coronawarn.server.services.federation.upload.payload.signing.BatchSigner;
import app.coronawarn.server.services.federation.upload.payload.signing.CryptoProvider;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


class UploadTest {

  @Nested
  @EnableConfigurationProperties(value = UploadServiceConfig.class)
  @ExtendWith(SpringExtension.class)
  @ContextConfiguration(classes = {
      Upload.class, PayloadFactory.class, DiagnosisKeyBatchAssembler.class,
      BatchSigner.class, CryptoProvider.class, DiagnosisKeyGenerator.class,
      ProdFederationUploadClient.class, DiagnosisKeyPersistenceLoader.class,
      FederationUploadKeyService.class, ValidDiagnosisKeyFilter.class
  },
      initializers = ConfigFileApplicationContextInitializer.class)
  class MockedUpload {
    @MockBean
    FederationGatewayClient federationGatewayClient;

    @Autowired
    private Upload upload;

    @MockBean
    private FederationUploadKeyRepository uploadKeyRepository;

    @SpyBean
    private DiagnosisKeyBatchAssembler batchAssembler;

    @Test
    void shouldRunUpload() throws Exception {
      upload.run(null);
      Mockito.verify(federationGatewayClient, Mockito.atMostOnce())
          .postBatchUpload(any(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void batchesShouldBeCreatedFromPendingUploadKeys() throws Exception {
      List<DiagnosisKey> testKeys = generateRandomDiagnosisKeys(true, 20);
      Mockito.when(uploadKeyRepository.findAllUploadableKeys()).thenReturn(testKeys);
      upload.run(null);
      verify(batchAssembler, times(1)).assembleDiagnosisKeyBatch(testKeys);
    }
  }

  @Nested
  @EnableConfigurationProperties(value = UploadServiceConfig.class)
  @ExtendWith(SpringExtension.class)
  @ActiveProfiles({"testdata", "fake-client"})
  @ContextConfiguration(classes = {
      Upload.class, PayloadFactory.class, DiagnosisKeyBatchAssembler.class,
      BatchSigner.class, CryptoProvider.class, DiagnosisKeyGenerator.class,
      TestFederationUploadClient.class, DiagnosisKeyGenerator.class
  }, initializers = ConfigFileApplicationContextInitializer.class)
  class TestDataUpload {

    @Autowired
    private Upload upload;

    @Test
    void shouldGenerateTestKeys() throws Exception {
      upload.run(null);
//      verify(diagnosisKeyGenerator, times(1)).loadDiagnosisKeys();
    }

  }

}
