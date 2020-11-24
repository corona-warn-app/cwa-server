package app.coronawarn.server.common.federation.client;

import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FederationFeignClientConfiguration {

  private final FeignClientProvider feignClientProvider;

  public FederationFeignClientConfiguration(
      FeignClientProvider feignClientProvider) {
    this.feignClientProvider = feignClientProvider;
  }

  @Bean
  public Client feignClient() {
    return feignClientProvider.createFeignClient();
  }

}
