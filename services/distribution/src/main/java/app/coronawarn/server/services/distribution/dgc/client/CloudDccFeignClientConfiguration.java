
package app.coronawarn.server.services.distribution.dgc.client;

import feign.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!fake-dcc-client")
public class CloudDccFeignClientConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(CloudDccFeignClientConfiguration.class);

  private final CloudDccFeignHttpClientProvider feignClientProvider;

  public CloudDccFeignClientConfiguration(CloudDccFeignHttpClientProvider feignClientProvider) {
    this.feignClientProvider = feignClientProvider;
  }

  @Bean
  public Client feignClient() {
    return feignClientProvider.createFeignClient();
  }
}
