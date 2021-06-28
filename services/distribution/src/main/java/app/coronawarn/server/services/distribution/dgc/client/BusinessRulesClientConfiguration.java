
package app.coronawarn.server.services.distribution.dgc.client;

import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!fake-dcc-client")
public class BusinessRulesClientConfiguration {

  private final CloudFeignBusinessRulesClientProvider feignClientProvider;

  public BusinessRulesClientConfiguration(CloudFeignBusinessRulesClientProvider feignClientProvider) {
    this.feignClientProvider = feignClientProvider;
  }

  @Bean
  public Client feignClient() {
    return feignClientProvider.createFeignClient();
  }

}
