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
import app.coronawarn.server.services.federation.upload.payload.helper.DiagnosisKeyBatchGenerator;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.util.Arrays;

import static app.coronawarn.server.services.federation.upload.payload.helper.FakePrivateKeyResource.makeFakeResourceLoader;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class FakePrivateKey implements PrivateKey {

  private final String algorithm;
  private final String format;
  private final byte[] encoded;
  private final boolean destroyed;

  public FakePrivateKey(String algorithm, String format, byte[] encoded) {
    this.algorithm = algorithm;
    this.format = format;
    this.encoded = encoded;
    this.destroyed = false;
  }

  public FakePrivateKey(String algorithm, String format, byte[] encoded, boolean isDestroyed) {
    this.algorithm = algorithm;
    this.format = format;
    this.encoded = encoded;
    this.destroyed = isDestroyed;
  }

  @Override
  public String getAlgorithm() {
    return this.algorithm;
  }

  @Override
  public String getFormat() {
    return this.format;
  }

  @Override
  public byte[] getEncoded() {
    return this.encoded;
  }

  @Override
  public boolean isDestroyed() {
    return this.destroyed;
  }
}


class BatchSignerTest {

  @Nested
  @DisplayName("Real Crypto Tests")
  @EnableConfigurationProperties(value = UploadServiceConfig.class)
  @ExtendWith(SpringExtension.class)
  @ContextConfiguration(classes = {UploadServiceConfig.class}, initializers = ConfigFileApplicationContextInitializer.class)
  class RealTest {

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
      var result = batchSigner.createSignatureBytes(DiagnosisKeyBatch.newBuilder().build());
      Assertions.assertNotNull(result);
    }

    @Test
    void shouldSignBatchesDifferently()
        throws GeneralSecurityException, CMSException, OperatorCreationException, IOException {
      var signature1 = batchSigner.createSignatureBytes(DiagnosisKeyBatchGenerator.makeSingleKeyBatch());
      var signature2 = batchSigner.createSignatureBytes(DiagnosisKeyBatchGenerator.makeSingleKeyBatch());
      Assertions.assertNotEquals(signature1, signature2);
    }
  }

}
