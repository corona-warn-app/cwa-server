package app.coronawarn.server.services.distribution.dgc.dsc;

import static app.coronawarn.server.common.shared.util.CwaStringUtils.emptyCharrArrayIfNull;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.Client.Ssl;
import feign.Client;
import feign.httpclient.ApacheHttpClient;
import java.io.File;
import javax.net.ssl.SSLContext;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Creates a dedicated http client used by Feign when performing http calls to the Digital Signing Certificates Service.
 */
@Component
@Profile("!fake-dsc-client")
public class CloudDscFeignHttpClientProvider implements DscFeignHttpClientProvider {

  private static final Logger logger = LoggerFactory.getLogger(CloudDscFeignHttpClientProvider.class);

  private final Integer connectionPoolSize;

  /**
   * Construct Provider.
   *
   * @param config - distribution configuration
   */
  public CloudDscFeignHttpClientProvider(DistributionServiceConfig config) {
    this.connectionPoolSize = config.getConnectionPoolSize();
  }

  /**
   * Creates a FeignClient.
   */
  @Override
  @Bean
  public Client createFeignClient() {
    return new ApacheHttpClient(
        federationHttpClientFactory().createBuilder().build());
  }

  /**
   * Creates an {@link ApacheHttpClientFactory} that with no SSL certificates and no host names.
   */
  @Bean
  private ApacheHttpClientFactory federationHttpClientFactory() {
    return new DefaultApacheHttpClientFactory(HttpClientBuilder.create()
        .setMaxConnPerRoute(connectionPoolSize)
        .setMaxConnTotal(connectionPoolSize));
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
