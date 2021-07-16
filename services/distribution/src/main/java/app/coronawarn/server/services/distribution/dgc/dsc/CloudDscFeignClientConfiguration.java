
package app.coronawarn.server.services.distribution.dgc.dsc;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import feign.Client;
import feign.Retryer;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableFeignClients
@Profile("!fake-dsc-client")
public class CloudDscFeignClientConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(CloudDscFeignClientConfiguration.class);

  private final CloudDscFeignHttpClientProvider feignClientProvider;

  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Create an instance.
   */
  public CloudDscFeignClientConfiguration(CloudDscFeignHttpClientProvider feignClientProvider,
                                          DistributionServiceConfig distributionServiceConfig) {
    logger.debug("Creating Cloud DSC Feign Client Configuration");
    this.feignClientProvider = feignClientProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  @Bean
  public Client feignClient() {
    return feignClientProvider.createFeignClient();
  }

  /**
   * Retrier configuration for Feign DSC client.
   */
  @Bean
  public Retryer retryer() {
    long retryPeriod = TimeUnit.SECONDS.toMillis(
        distributionServiceConfig.getDigitalGreenCertificate().getDscClient().getRetryPeriod());

    long maxRetryPeriod = TimeUnit.SECONDS.toMillis(
        distributionServiceConfig.getDigitalGreenCertificate().getDscClient().getMaxRetryPeriod());

    int maxAttempts = distributionServiceConfig.getDigitalGreenCertificate().getDscClient().getMaxRetryAttempts();

    return new Retryer.Default(retryPeriod, maxRetryPeriod, maxAttempts);
  }

}