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
  private final File trustStore;
  private final String trustStorePassword;

  /**
   * Creates an {@link ApacheHttpClientFactory} that with no SSL certificates and no host names.
   */
  @Bean
  @Profile("dsc-client-factory")
  private ApacheHttpClientFactory dscFederationHttpClientFactory() {
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
  public CloudDscFeignHttpClientProvider(DistributionServiceConfig config) {
    Ssl ssl = config.getDigitalGreenCertificate().getDscClient().getSsl();

    this.connectionPoolSize = config.getConnectionPoolSize();
    this.trustStore = ssl.getTrustStore();
    this.trustStorePassword = ssl.getTrustStorePassword();
  }

  /**
   * Creates a FeignClient.
   */
  @Override
  @Bean
  public Client createDscFeignClient() {
    return new ApacheHttpClient(
        dscFederationHttpClientFactory().createBuilder().build());
  }

  /**
   * Creates connection manager.
   *
   * @return ApacheHttpClientConnectionManagerFactory.
   */
  private SSLContext getSslContext(File trustStorePath, String trustStorePass) {
    logger.info("Instantiating DSC client - SSL context with truststore: {}", trustStorePath.getName());
    try {
      return SSLContextBuilder.create().loadTrustMaterial(trustStorePath,
          emptyCharrArrayIfNull(trustStorePass))
          .build();
    } catch (Exception e) {
      logger.error("Problem on creating DSC client - SSL context with truststore: "
          + trustStorePath.getName(), e);
      throw new RuntimeException(e);
    }
  }
}
