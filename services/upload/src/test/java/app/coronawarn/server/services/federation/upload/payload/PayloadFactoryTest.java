

package app.coronawarn.server.services.federation.upload.payload;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import app.coronawarn.server.services.federation.upload.payload.signing.BatchSigner;
import app.coronawarn.server.services.federation.upload.utils.BatchMockData;
import app.coronawarn.server.services.federation.upload.utils.MockData;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@EnableConfigurationProperties(value = UploadServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PayloadFactory.class, AllowedPropertiesMap.class},
    initializers = ConfigFileApplicationContextInitializer.class)
class PayloadFactoryTest {

  @MockBean
  DiagnosisKeyBatchAssembler mockAssembler;

  @MockBean
  BatchSigner mockSigner;

  @Autowired
  PayloadFactory payloadFactory;

  @BeforeEach
  void setup() throws GeneralSecurityException, CMSException, OperatorCreationException, IOException {
    String signature = new String();
//    var random = new Random();
//    random.nextBytes(signatureBytes);
    when(mockSigner.createSignatureBytes(any()))
        .thenReturn(signature);
  }

  @Test
  void shouldMakePayloadFromListOfDiagnosisKeys() {
    var diagnosisKeys = List.of(MockData.generateRandomUploadKey(true));

    when(mockAssembler.assembleDiagnosisKeyBatch(anyList()))
        .thenReturn(Map.of(BatchMockData.makeSingleKeyBatch(), diagnosisKeys));

    var result = payloadFactory.makePayloadList(diagnosisKeys);
    Assertions.assertEquals(1, result.size());
    Assertions.assertNotNull(result.get(0).getBatch());
    Assertions.assertNotNull(result.get(0).getBatchSignature());
    Assertions.assertNotNull(result.get(0).getBatchTag());
  }

  @Test
  void payloadsShouldNotHaveSameBatchTag() {
    var diagnosisKeys = List.of(MockData.generateRandomUploadKey(true));

    when(mockAssembler.assembleDiagnosisKeyBatch(anyList()))
        .thenReturn(Map.of(
            BatchMockData.makeSingleKeyBatch(), diagnosisKeys,
            BatchMockData.makeSingleKeyBatch(), diagnosisKeys,
            BatchMockData.makeSingleKeyBatch(), diagnosisKeys));

    var result = payloadFactory.makePayloadList(diagnosisKeys);
    Assertions.assertEquals(3, result.size());
    Assertions.assertArrayEquals(
        result.stream().distinct().toArray(),
        result.toArray(),
        "All payload objects should have different batchTag"
    );
  }

}
