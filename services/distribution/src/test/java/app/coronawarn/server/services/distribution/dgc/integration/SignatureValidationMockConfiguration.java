package app.coronawarn.server.services.distribution.dgc.integration;

import app.coronawarn.server.services.distribution.dgc.client.signature.DccSignatureValidator;
import java.io.IOException;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
public class SignatureValidationMockConfiguration {

  @Bean
  @Primary
  @Profile("integration-test")
  public DccSignatureValidator getDccSignatureValidator() {
    return new DccSignatureValidator(null) {
      @Override
      public void checkSignature(String signature, String body) throws IOException {
        //do nothing, as we are using this in unit tests where we don't have a valid signature.
      }
    };
  }
}
