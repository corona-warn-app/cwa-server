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

package app.coronawarn.server.services.federation.upload.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.federation.client.upload.BatchUploadResponse;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.common.persistence.service.FederationUploadKeyService;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import app.coronawarn.server.common.persistence.service.common.ValidDiagnosisKeyFilter;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import app.coronawarn.server.services.federation.upload.keys.DiagnosisKeyLoader;
import app.coronawarn.server.services.federation.upload.payload.DiagnosisKeyBatchAssembler;
import app.coronawarn.server.services.federation.upload.payload.PayloadFactory;
import app.coronawarn.server.services.federation.upload.payload.signing.BatchSigner;
import app.coronawarn.server.services.federation.upload.payload.signing.CryptoProvider;
import app.coronawarn.server.services.federation.upload.runner.Upload;
import app.coronawarn.server.services.federation.upload.utils.MockData;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = UploadServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Upload.class, PayloadFactory.class, DiagnosisKeyBatchAssembler.class,
    BatchSigner.class, CryptoProvider.class, FederationUploadKeyService.class, ValidDiagnosisKeyFilter.class,
    KeySharingPoliciesChecker.class, FederationGatewayClient.class},
    initializers = ConfigFileApplicationContextInitializer.class)
class ProdFederationUploadClientTest {

  @MockBean
  ProdFederationUploadClient mockProdUploadClient;

  @MockBean
  DiagnosisKeyLoader mockDiagnosisKeyLoader;

  @MockBean
  FederationGatewayClient federationGatewayClient;

  @MockBean
  FederationUploadKeyService federationUploadKeyService;

  @SpyBean
  UploadServiceConfig uploadServiceConfig;

  @Autowired
  Upload upload;

  @Test
  void checkStatusCodesAreEmptyForEmptyUploadResponse() throws Exception {
    var testKey1 = MockData.generateRandomUploadKey(true);
    var testKey2 = MockData.generateRandomUploadKey(true);

    when(uploadServiceConfig.getMinBatchKeyCount()).thenReturn(2);
    when(mockDiagnosisKeyLoader.loadDiagnosisKeys()).thenReturn(List.of(testKey1, testKey2));
    when(federationGatewayClient.postBatchUpload(any(), any(), any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.ACCEPTED));
    when(mockProdUploadClient.postBatchUpload(any())).thenReturn(new BatchUploadResponse());
    upload.run(null);

    verify(mockProdUploadClient, atLeastOnce()).postBatchUpload(any());
  }
}
