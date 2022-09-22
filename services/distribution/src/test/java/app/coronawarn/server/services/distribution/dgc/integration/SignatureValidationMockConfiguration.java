package app.coronawarn.server.services.distribution.dgc.integration;

import app.coronawarn.server.services.distribution.dgc.client.signature.DccSignatureValidator;
import org.mockito.Mock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import java.io.IOException;

@TestConfiguration
public class SignatureValidationMockConfiguration {

  @Bean
  @Primary
  @Profile("integration-test")
  public DccSignatureValidator getDccSignatureValidator() {
    return new DccSignatureValidator(null) {
      @Override
      public void checkSignature(String signature, String body) throws IOException {

      }
    };
  }
}
