package app.coronawarn.server.services.federation.upload.payload;

import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import app.coronawarn.server.services.federation.upload.payload.helper.DiagnosisKeyBatchGenerator;
import app.coronawarn.server.services.federation.upload.payload.helper.PersistenceKeysGenerator;
import app.coronawarn.server.services.federation.upload.payload.signing.BatchSigner;
import app.coronawarn.server.services.federation.upload.payload.signing.CryptoProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@EnableConfigurationProperties(value = UploadServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PayloadFactory.class}, initializers = ConfigFileApplicationContextInitializer.class)
class PayloadFactoryTest {

  @MockBean
  DiagnosisKeyBatchAssembler mockAssembler;

  @MockBean
  BatchSigner mockSigner;

  @Autowired
  PayloadFactory payloadFactory;

  @BeforeEach
  void setup() throws GeneralSecurityException {
    byte[] signatureBytes = new byte[32];
    var random = new Random();
    random.nextBytes(signatureBytes);
    when(mockSigner.createSignatureBytes(any()))
        .thenReturn(signatureBytes);
  }

  @Test
  void shouldMakePayloadFromListOfDiagnosisKeys() {
    var diagnosisKeys = List.of(PersistenceKeysGenerator.makeDiagnosisKey());

    when(mockAssembler.assembleDiagnosisKeyBatch(anyList()))
        .thenReturn(List.of(DiagnosisKeyBatchGenerator.makeSingleKeyBatch()));

    var result = payloadFactory.makePayloadList(diagnosisKeys);
    Assertions.assertEquals(1, result.size());
    Assertions.assertNotNull(result.get(0).getBatch());
    Assertions.assertNotNull(result.get(0).getBatchSignature());
    Assertions.assertNotNull(result.get(0).getBatchTag());
  }

  @Test
  void payloadsShouldHaveSameBatchTag() {
    var diagnosisKeys = List.of(PersistenceKeysGenerator.makeDiagnosisKey());

    when(mockAssembler.assembleDiagnosisKeyBatch(anyList()))
        .thenReturn(List.of(
            DiagnosisKeyBatchGenerator.makeSingleKeyBatch(),
            DiagnosisKeyBatchGenerator.makeSingleKeyBatch(),
            DiagnosisKeyBatchGenerator.makeSingleKeyBatch()));

    var result = payloadFactory.makePayloadList(diagnosisKeys);
    Assertions.assertEquals(3, result.size());
    Assertions.assertTrue(
        result.stream().allMatch(b -> b.getBatchTag().equals(result.get(0).getBatchTag())),
        "All payload objects should have same batchTag");
  }

}
