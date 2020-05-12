package app.coronawarn.server.services.distribution;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.distribution.crypto.CryptoProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class ApplicationInitializationTest {

  @Autowired
  private DiagnosisKeyService diagnosisKeyService;

  @Autowired
  private CryptoProvider cryptoProvider;

  @Test
  public void beansInitialized() {
    assertNotNull(diagnosisKeyService);
    assertNotNull(cryptoProvider);
  }
}
