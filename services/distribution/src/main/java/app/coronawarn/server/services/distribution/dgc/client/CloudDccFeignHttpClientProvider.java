package app.coronawarn.server.services.distribution.dgc.client;

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
 * Creates a dedicated http client used by Feign when performing http calls to the Federation Gateway Service.
 */
@Component
@Profile("!fake-dcc-client")
public class CloudDccFeignHttpClientProvider implements DccFeignHttpClientProvider {

  private static final Logger logger = LoggerFactory.getLogger(CloudDccFeignHttpClientProvider.class);

  private final Integer connectionPoolSize;
  private final File trustStore;
  private final String trustStorePassword;

  /**
   * Construct Provider.
   *
   * @param config - distribution configuration
   */
  public CloudDccFeignHttpClientProvider(DistributionServiceConfig config) {

    Ssl ssl = config.getDigitalGreenCertificate().getClient().getSsl();
    this.connectionPoolSize = config.getConnectionPoolSize();
    this.trustStore = ssl.getTrustStore();
    this.trustStorePassword = ssl.getTrustStorePassword();
  }

  /**
   * Creates a FeignClient.
   */
  @Override
  @Bean
  public Client createFeignClient() {
    return new ApacheHttpClient(
        dccHttpClientFactory().createBuilder().build());
  }

  /**
   * Creates an {@link ApacheHttpClientFactory} that with no SSL certificates and no host names.
   */
  @Bean
  @Profile("dcc-client-factory")
  private ApacheHttpClientFactory dccHttpClientFactory() {
    return new DefaultApacheHttpClientFactory(HttpClientBuilder.create()
        .setMaxConnPerRoute(connectionPoolSize)
        .setMaxConnTotal(connectionPoolSize)
        .setSSLContext(getSslContext(this.trustStore, this.trustStorePassword)));
  }

  private SSLContext getSslContext(File trustStorePath, String trustStorePass) {
    logger.info("Instantiating DCC client - SSL context with truststore: {}", trustStorePath.getName());
    try {
      return SSLContextBuilder.create().loadTrustMaterial(trustStorePath,
              emptyCharrArrayIfNull(trustStorePass))
          .build();
    } catch (Exception e) {
      logger.error("Problem on creating SSL context with truststore: {}", trustStorePath.getName(), e);
      throw new RuntimeException(e);
    }
  }
}
