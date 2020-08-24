package app.coronawarn.server.services.federation.upload.runner;

import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import app.coronawarn.server.services.federation.upload.signing.CryptoProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = UploadServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Upload.class, CryptoProvider.class}, initializers = ConfigFileApplicationContextInitializer.class)
class UploadTest {

  @Autowired
  private Upload upload;

  @Test
  void shouldRunUpload() throws Exception {
    upload.run(null);
  }

}
