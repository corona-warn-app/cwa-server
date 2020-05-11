package app.coronawarn.server.services.distribution;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import app.coronawarn.server.services.common.persistence.service.DiagnosisKeyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ApplicationInitializationTest {

  @Autowired
  private DiagnosisKeyService diagnosisKeyService;

  @Test
  public void jpaComponentsInjected() {
    assertNotNull(diagnosisKeyService);
  }
}
