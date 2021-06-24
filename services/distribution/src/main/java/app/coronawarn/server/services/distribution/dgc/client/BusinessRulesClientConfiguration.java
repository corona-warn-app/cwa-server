
package app.coronawarn.server.services.distribution.dgc.client;

import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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
