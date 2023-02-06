package app.coronawarn.server.services.distribution.dgc.dsc;

import app.coronawarn.server.common.federation.client.AbstractFeignClientProvider;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Creates a dedicated http client used by Feign when performing http calls to the Digital Signing Certificates Service.
 */
@Component
@Profile({ "!fake-dsc-client", "!revocation" })
// @Profile({ "dsc-client-factory", "!revocation" })
public class CloudDscFeignHttpClientProvider extends AbstractFeignClientProvider implements DscFeignHttpClientProvider {

  /**
   * Construct Provider.
   *
   * @param config - distribution configuration
   */
  public CloudDscFeignHttpClientProvider(final DistributionServiceConfig config) {
    super(config.getConnectionPoolSize(), config.getDigitalGreenCertificate().getDscClient().getSsl().getTrustStore(),
        config.getDigitalGreenCertificate().getDscClient().getSsl().getTrustStorePassword());
  }

  /**
   * Creates a FeignClient.
   */
  @Override
  @Bean
  @Profile("!revocation")
  public Client createDscFeignClient() {
    return createFeignClient();
  }
}
