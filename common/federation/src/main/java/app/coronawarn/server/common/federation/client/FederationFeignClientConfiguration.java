package app.coronawarn.server.common.federation.client;

import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FederationFeignClientConfiguration {

  private final FederationFeignHttpClientProvider federationFeignHttpClientProvider;

  public FederationFeignClientConfiguration(
      FederationFeignHttpClientProvider federationFeignHttpClientProvider) {
    this.federationFeignHttpClientProvider = federationFeignHttpClientProvider;
  }

  @Bean
  public Client feignClient() {
    return federationFeignHttpClientProvider.createFeignClient();
  }

}
