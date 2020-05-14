package app.coronawarn.server.common.persistence;

import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
public class TestApplication {
  @Bean
  DiagnosisKeyService createDiagnosisKeyService() {
    return new DiagnosisKeyService();
  }
}
