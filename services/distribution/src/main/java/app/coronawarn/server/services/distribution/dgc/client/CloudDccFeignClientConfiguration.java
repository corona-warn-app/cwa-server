
package app.coronawarn.server.services.distribution.dgc.client;

import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class CloudDccFeignClientConfiguration {

  private final CloudDccFeignHttpClientProvider feignClientProvider;

  public CloudDccFeignClientConfiguration(CloudDccFeignHttpClientProvider feignClientProvider) {
    this.feignClientProvider = feignClientProvider;
  }

  @Bean
  public Client feignClient() {
    return feignClientProvider.createFeignClient();
  }
}
