package app.coronawarn.server.services.distribution.dgc.client;

import app.coronawarn.server.common.federation.client.AbstractFeignClientProvider;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.client.signature.DccFeignDelegator;
import app.coronawarn.server.services.distribution.dgc.client.signature.DccSignatureValidator;
import feign.Client;
import feign.Request;
import feign.Request.Options;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Creates a dedicated http client used by Feign when performing http calls to the Federation Gateway Service.
 */
@Component
@Profile("!revocation")
public class CloudDccFeignHttpClientProvider extends AbstractFeignClientProvider implements DccFeignHttpClientProvider {

  private final DccSignatureValidator dccSignatureValidator;

  /**
   * Construct Provider.
   *
   * @param config - distribution configuration
   */
  public CloudDccFeignHttpClientProvider(DistributionServiceConfig config,
      DccSignatureValidator dccSignatureValidator) {
    super(config.getConnectionPoolSize(), config.getDigitalGreenCertificate().getClient().getSsl().getTrustStore(),
        config.getDigitalGreenCertificate().getClient().getSsl().getTrustStorePassword());

    this.dccSignatureValidator = dccSignatureValidator;
  }

  /**
   * Creates a FeignClient. {@link DccFeignDelegator} is used to intercept the response before
   * {@link ApacheHttpClient#execute(Request, Options)} in order to validate the signature.
   */
  @Override
  @Bean
  @Profile("!revocation")
  public Client createFeignClient() {
    return new DccFeignDelegator(super.createFeignClient(), this.dccSignatureValidator);
  }
}
