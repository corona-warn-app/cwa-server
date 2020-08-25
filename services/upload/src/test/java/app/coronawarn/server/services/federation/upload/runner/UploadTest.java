package app.coronawarn.server.services.federation.upload.runner;

import app.coronawarn.server.services.federation.upload.client.FederationUploadClient;
import app.coronawarn.server.services.federation.upload.client.TestFederationUploadClient;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import app.coronawarn.server.services.federation.upload.keys.DiagnosisKeyGenerator;
import app.coronawarn.server.services.federation.upload.payload.DiagnosisKeyBatchAssembler;
import app.coronawarn.server.services.federation.upload.payload.PayloadFactory;
import app.coronawarn.server.services.federation.upload.payload.signing.BatchSigner;
import app.coronawarn.server.services.federation.upload.payload.signing.CryptoProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.ArgumentMatchers.any;

@EnableConfigurationProperties(value = UploadServiceConfig.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    Upload.class, PayloadFactory.class, DiagnosisKeyBatchAssembler.class,
    BatchSigner.class, CryptoProvider.class, DiagnosisKeyGenerator.class
})
@ActiveProfiles({"testdata"})
class UploadTest {

  @MockBean
  FederationUploadClient federationUploadClient;

  @Autowired
  private Upload upload;

  @Test
  void shouldRunUpload() {
    Mockito.verify(federationUploadClient, Mockito.atMostOnce()).postBatchUpload(any());
  }

}
