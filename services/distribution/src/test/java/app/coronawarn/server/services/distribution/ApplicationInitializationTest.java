package app.coronawarn.server.services.distribution;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.distribution.crypto.CryptoProvider;
import app.coronawarn.server.services.distribution.objectstore.S3Publisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Application.class},
    initializers = ConfigFileApplicationContextInitializer.class)
public class ApplicationInitializationTest {

  @Autowired
  private DiagnosisKeyService diagnosisKeyService;

  @Autowired
  private CryptoProvider cryptoProvider;

  @Autowired
  private S3Publisher s3Publisher;

  @Test
  public void beansInitialized() {
    assertNotNull(diagnosisKeyService);
    assertNotNull(cryptoProvider);
    assertNotNull(s3Publisher);
  }
}
