package app.coronawarn.server.services.distribution.dgc.client;

import app.coronawarn.server.common.federation.client.hostname.HostnameVerifierProvider;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import feign.Client;
import feign.httpclient.ApacheHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Creates a dedicated http client used by Feign when performing http calls to the Federation Gateway Service.
 */
@Component
@Profile("!fake-dcc-client")
public class CloudDccFeignHttpClientProvider implements DccFeignHttpClientProvider {

  private final Integer connectionPoolSize;

  private final HostnameVerifierProvider hostnameVerifierProvider;

  /**
   * Construct Provider.
   *
   * @param config - distribution configuration
   */
  public CloudDccFeignHttpClientProvider(DistributionServiceConfig config,
      HostnameVerifierProvider hostnameVerifierProvider) {
    this.connectionPoolSize = config.getConnectionPoolSize();
    this.hostnameVerifierProvider = hostnameVerifierProvider;
  }

  /**
   * Creates a FeignClient.
   */
  @Override
  @Bean
  public Client createFeignClient() {
    return new ApacheHttpClient(
        federationHttpClientFactory(connectionPoolSize).createBuilder().build());
  }

  /**
   * Creates an {@link ApacheHttpClientFactory} that with no SSL certificates and no host names.
   */
  private ApacheHttpClientFactory federationHttpClientFactory(int connectionPoolSize) {
    return new DefaultApacheHttpClientFactory(HttpClientBuilder.create()
        .setMaxConnPerRoute(connectionPoolSize)
        .setMaxConnTotal(connectionPoolSize)
        .setSSLHostnameVerifier(hostnameVerifierProvider.createHostnameVerifier()));
  }

  /**
   * Creates connection manager.
   *
   * @return ApacheHttpClientConnectionManagerFactory.
   */
  @Bean
  public ApacheHttpClientConnectionManagerFactory createConnectionManager() {
    return new DefaultApacheHttpClientConnectionManagerFactory();
  }
}
