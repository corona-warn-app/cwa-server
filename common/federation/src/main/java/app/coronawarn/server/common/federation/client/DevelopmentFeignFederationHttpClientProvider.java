package app.coronawarn.server.common.federation.client;

import app.coronawarn.server.common.federation.client.config.FederationGatewayConfig;
import feign.Client;
import feign.httpclient.ApacheHttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("disable-ssl-efgs-verification")
public class DevelopmentFeignFederationHttpClientProvider implements FeignFederationHttpClientProvider {

  private static final Logger logger = LoggerFactory.getLogger(DevelopmentFeignFederationHttpClientProvider.class);

  private final Integer connectionPoolSize;

  /**
   * Construct Provider.
   *
   * @param config .
   */
  public DevelopmentFeignFederationHttpClientProvider(FederationGatewayConfig config) {
    logger.warn("The Federation Http Client is started with SSL disabled. This should never be used in PRODUCTION!");
    this.connectionPoolSize = config.getConnectionPoolSize();
  }

  /**
   * Creates a FeignClient.
   */
  @Override
  @Bean
  public Client createFeignClient() {
    return new ApacheHttpClient(federationHttpClientFactory(connectionPoolSize).createBuilder().build());
  }

  /**
   * Creates an {@link ApacheHttpClientFactory} that validates SSL certificates
   * but no host names.
   */
  private ApacheHttpClientFactory federationHttpClientFactory(int connectionPoolSize) {
    return new DefaultApacheHttpClientFactory(HttpClientBuilder.create()
        .setMaxConnPerRoute(connectionPoolSize)
        .setMaxConnTotal(connectionPoolSize)
        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE));
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
