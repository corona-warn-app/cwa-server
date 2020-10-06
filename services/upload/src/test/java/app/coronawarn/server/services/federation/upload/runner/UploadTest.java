

package app.coronawarn.server.services.federation.upload.runner;

import app.coronawarn.server.services.federation.upload.client.TestFederationUploadClient;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@EnableConfigurationProperties(value = UploadServiceConfig.class)
@ActiveProfiles({"testdata", "fake-client"})
@DirtiesContext
@SpringBootTest
class UploadTest {

  @SpyBean
  TestFederationUploadClient spyFederationClient;

  @Test
  void shouldGenerateTestKeys() {
    verify(spyFederationClient, times(2)).postBatchUpload(any());
  }

}
