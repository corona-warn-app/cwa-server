package app.coronawarn.server.services.federation.upload.runner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Upload.class}, initializers = ConfigFileApplicationContextInitializer.class)
class UploadTest {

  @Autowired
  private Upload upload;

  @Test
  void shouldRunUpload() throws Exception {
    upload.run(null);
  }

}
