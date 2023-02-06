package app.coronawarn.server.services.distribution.dcc;

import app.coronawarn.server.common.federation.client.AbstractFeignClientProvider;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dsc-rev-client-factory")
@Component
public class CloudDccRevocationFeignHttpClientProvider extends AbstractFeignClientProvider
    implements DccRevocationFeignHttpClientProvider {

  /**
   * Construct Provider.
   *
   * @param config - distribution configuration
   */
  public CloudDccRevocationFeignHttpClientProvider(final DistributionServiceConfig config) {
    super(config.getConnectionPoolSize(), config.getDccRevocation().getClient().getSsl().getTrustStore(),
        config.getDccRevocation().getClient().getSsl().getTrustStorePassword());
  }

  /**
   * Creates a FeignClient.
   */
  @Profile("dsc-rev-client-factory")
  @Override
  @Bean
  public Client createDccRevocationFeignClient() {
    return new DccRevocationClientDelegator(createFeignClient());
  }
}
