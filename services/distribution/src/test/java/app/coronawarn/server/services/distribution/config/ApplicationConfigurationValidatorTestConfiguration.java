

package app.coronawarn.server.services.distribution.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("applicationConfigurationValidatorTest")
public class ApplicationConfigurationValidatorTestConfiguration {
  @Bean
  @Primary
  public DistributionServiceConfig distributionServiceConfigSpy(DistributionServiceConfig distributionServiceConfig) {
    return Mockito.spy(distributionServiceConfig);
  }
}
