package app.coronawarn.server.services.distribution.dcc;

import static app.coronawarn.server.common.shared.util.CwaStringUtils.emptyCharrArrayIfNull;

import app.coronawarn.server.common.federation.client.CloudFeignHttpClientProviderException;
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
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("revocation")
public class CloudDccRevocationFeignHttpClientProvider implements DccRevocationFeignHttpClientProvider {

  private static final Logger logger = LoggerFactory.getLogger(CloudDccRevocationFeignHttpClientProvider.class);

  private final Integer connectionPoolSize;
  private final File trustStore;
  private final String trustStorePassword;

  /**
   * Creates an {@link ApacheHttpClientFactory} that with no SSL certificates and no host names.
   */
  @Bean
  @Profile("dsc-rev-client-factory")
  private ApacheHttpClientFactory dccHttpClientFactory() {
    return new DefaultApacheHttpClientFactory(HttpClientBuilder.create()
        .setMaxConnPerRoute(connectionPoolSize)
        .setMaxConnTotal(connectionPoolSize)
        .setSSLContext(getSslContext(this.trustStore, this.trustStorePassword)));
  }

  /**
   * Construct Provider.
   *
   * @param config - distribution configuration
   */
  public CloudDccRevocationFeignHttpClientProvider(DistributionServiceConfig config) {
    Ssl ssl = config.getDccRevocation().getClient().getSsl();

    this.connectionPoolSize = config.getConnectionPoolSize();
    this.trustStore = ssl.getTrustStore();
    this.trustStorePassword = ssl.getTrustStorePassword();
  }

  /**
   * Creates a FeignClient.
   */
  @Override
  @Bean
  public Client createDccRevocationFeignClient() {
    return new DccRevocationClientDelegator(new ApacheHttpClient(dccHttpClientFactory().createBuilder().build()));
  }

  /**
   * Creates connection manager.
   *
   * @return ApacheHttpClientConnectionManagerFactory.
   */
  private SSLContext getSslContext(File trustStorePath, String trustStorePass) {
    logger.info("Instantiating DCC Revocation client - SSL context with truststore: {}", trustStorePath.getName());
    try {
      return SSLContextBuilder.create().loadTrustMaterial(trustStorePath,
          emptyCharrArrayIfNull(trustStorePass))
          .build();
    } catch (Exception e) {
      logger.error("Problem on creating DCC Revocation client - SSL context with truststore: "
          + trustStorePath.getName(), e);
      throw new CloudFeignHttpClientProviderException(e);
    }
  }
}
