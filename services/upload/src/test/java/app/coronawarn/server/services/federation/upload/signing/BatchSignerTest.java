package app.coronawarn.server.services.federation.upload.signing;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Random;

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
  @DisplayName("Mocked Crypto Tests")
  @EnableConfigurationProperties(value = UploadServiceConfig.class)
  @ExtendWith(SpringExtension.class)
  @ContextConfiguration(classes = {BatchSigner.class}, initializers = ConfigFileApplicationContextInitializer.class)
  class MockedTest {
    @MockBean
    private CryptoProvider cryptoProvider;

    @MockBean
    private UploadServiceConfig uploadServiceConfig;

    @Test
    void shouldCreateBatchSigner() {
      when(cryptoProvider.getPrivateKey()).thenReturn(new FakePrivateKey("1.2.840.10045.4.3.2", "SHA256withECDSA", new byte[254]));
      var signer = new BatchSigner(cryptoProvider, uploadServiceConfig);
    }

    @Test
    void shouldThrowErrorIfPrivateKeyIsNull() {
      when(cryptoProvider.getPrivateKey()).thenReturn(null);
      var signer = new BatchSigner(cryptoProvider, uploadServiceConfig);
      Assertions.assertThrows(GeneralSecurityException.class, () -> {
        signer.createSignatureBytes(DiagnosisKeyBatch.newBuilder().build());
      });
    }

    @Test
    void shouldThrowErrorIfPrivateKeyIsDestroyed() {
      when(cryptoProvider.getPrivateKey()).thenReturn(new FakePrivateKey("algo1", "format1", new byte[254], true));
      var signer = new BatchSigner(cryptoProvider, uploadServiceConfig);
      Assertions.assertThrows(GeneralSecurityException.class, () -> {
        signer.createSignatureBytes(DiagnosisKeyBatch.newBuilder().build());
      });
    }
  }

  @Nested
  @DisplayName("Real Crypto Tests")
  @EnableConfigurationProperties(value = UploadServiceConfig.class)
  @ExtendWith(SpringExtension.class)
  @SpringBootTest(classes = {BatchSigner.class, CryptoProvider.class })
//  @ContextConfiguration(classes = {BatchSigner.class}, initializers = ConfigFileApplicationContextInitializer.class)
  class RealTest {

    @Autowired
    private BatchSigner batchSigner;

    @Test
    void shouldSignBatchWithBouncyCastle() throws GeneralSecurityException {
      var result = batchSigner.createSignatureBytes(DiagnosisKeyBatch.newBuilder().build());
      Assertions.assertNotNull(result);
    }

    @Test
    void shouldSignBatchesDifferently() throws GeneralSecurityException {
      Random random = new Random();
      byte[] bytes = new byte[16];
      random.nextBytes(bytes);
      var batch1 = DiagnosisKeyBatch.newBuilder()
          .addKeys(DiagnosisKey.newBuilder().setKeyData(ByteString.copyFrom(bytes)).build()).build();
      random.nextBytes(bytes);
      var batch2 = DiagnosisKeyBatch.newBuilder()
          .addKeys(DiagnosisKey.newBuilder().setKeyData(ByteString.copyFrom(bytes)).build()).build();

      var signature1 = batchSigner.createSignatureBytes(batch1);
      var signature2 = batchSigner.createSignatureBytes(batch2);
      Assertions.assertFalse(Arrays.equals(signature1, signature2));
    }
  }

}
