
package app.coronawarn.server.services.distribution.dgc.client;

import app.coronawarn.server.services.distribution.assembly.component.DigitalGreenCertificateStructureProvider;
import feign.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class CloudDccFeignClientConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(CloudDccFeignClientConfiguration.class);

  private final CloudDccFeignHttpClientProvider feignClientProvider;

  public CloudDccFeignClientConfiguration(CloudDccFeignHttpClientProvider feignClientProvider) {
    logger.info("creating DCC feign client configuration");
    this.feignClientProvider = feignClientProvider;
  }

  @Bean
  public Client feignClient() {
    return feignClientProvider.createFeignClient();
  }
}
