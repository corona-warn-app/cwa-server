package app.coronawarn.server.common.federation.client;

import app.coronawarn.server.common.federation.client.config.FederationGatewayConfig;
import app.coronawarn.server.common.federation.client.hostname.HostnameVerifierProvider;
import org.springframework.stereotype.Component;

/**
 * Creates a dedicated http client used by Feign when performing http calls to the Federation Gateway Service.
 */
@Component
public class CloudFederationFeignHttpClientProvider extends AbstractFeignClientProvider
    implements FederationFeignHttpClientProvider {

  /**
   * Construct Provider.
   *
   * @param config .
   */
  public CloudFederationFeignHttpClientProvider(final FederationGatewayConfig config,
      final HostnameVerifierProvider hostnameVerifierProvider) {
    super(config.getConnectionPoolSize(), config.getSsl().getTrustStore(), config.getSsl().getTrustStorePassword(),
        config.getSsl().getKeyStore(), config.getSsl().getKeyStorePassword(), null, hostnameVerifierProvider);
  }
}
