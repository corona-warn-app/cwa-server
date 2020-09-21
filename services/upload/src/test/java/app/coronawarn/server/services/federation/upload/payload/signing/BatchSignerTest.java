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

package app.coronawarn.server.services.federation.upload.payload.signing;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import app.coronawarn.server.services.federation.upload.utils.BatchMockData;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.util.List;

import static app.coronawarn.server.services.federation.upload.utils.SecretResourceMockData.makeFakeResourceLoader;

@EnableConfigurationProperties(value = UploadServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UploadServiceConfig.class}, initializers = ConfigFileApplicationContextInitializer.class)
class BatchSignerTest {

  private BatchSigner batchSigner;

  @Autowired
  private UploadServiceConfig uploadServiceConfig;

  @BeforeEach
  void setup() throws IOException, CertificateException {
    var cryptoProvider = new CryptoProvider(makeFakeResourceLoader(), uploadServiceConfig);
    batchSigner = new BatchSigner(cryptoProvider, uploadServiceConfig);
  }

  @Test
  void shouldSignBatchWithBouncyCastle()
      throws GeneralSecurityException, CMSException, OperatorCreationException, IOException {
    var result = batchSigner.createSignatureBytes(
        DiagnosisKeyBatch.newBuilder().build());
    Assertions.assertNotNull(result);
  }

  @Test
  void shouldSignBatchesDifferently()
      throws GeneralSecurityException, CMSException, OperatorCreationException, IOException {
    var signature1 = batchSigner.createSignatureBytes(
            BatchMockData.makeSingleKeyBatch());
    var signature2 = batchSigner.createSignatureBytes(
            BatchMockData.makeSingleKeyBatch());
    Assertions.assertNotEquals(signature1, signature2);
  }

}
