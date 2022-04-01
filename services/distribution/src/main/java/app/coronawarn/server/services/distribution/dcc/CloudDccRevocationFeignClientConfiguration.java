package app.coronawarn.server.services.distribution.dcc;

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
@Profile("!fake-dcc-revocation")
public class CloudDccRevocationFeignClientConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(CloudDccRevocationFeignClientConfiguration.class);

  private final CloudDccRevocationFeignHttpClientProvider feignClientProvider;

  private final DistributionServiceConfig.Client clientConfig;

  /**
   * Create an instance.
   */
  public CloudDccRevocationFeignClientConfiguration(CloudDccRevocationFeignHttpClientProvider feignClientProvider,
      DistributionServiceConfig distributionServiceConfig) {
    logger.debug("Creating Cloud DCC Revocation Feign Client Configuration");
    this.feignClientProvider = feignClientProvider;
    this.clientConfig = distributionServiceConfig.getDccRevocation().getClient();
  }

  @Bean
  public Client dccFeignClient() {
    return feignClientProvider.createFeignClient();
  }

  /**
   * Retrier configuration for Feign DCC Revocation client.
   */
  @Bean
  public Retryer dccRevocationRetryer() {
    long retryPeriod = TimeUnit.SECONDS.toMillis(clientConfig.getRetryPeriod());
    long maxRetryPeriod = TimeUnit.SECONDS.toMillis(clientConfig.getMaxRetryPeriod());
    int maxAttempts = clientConfig.getMaxRetryAttempts();
    return new Retryer.Default(retryPeriod, maxRetryPeriod, maxAttempts);
  }
}
