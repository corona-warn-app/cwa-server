package app.coronawarn.server.common.federation.client;

import static app.coronawarn.server.common.shared.util.CwaStringUtils.emptyCharrArrayIfNull;

import app.coronawarn.server.common.federation.client.config.FederationGatewayConfig;
import app.coronawarn.server.common.federation.client.hostname.HostnameVerifierProvider;
import feign.Client;
import feign.httpclient.ApacheHttpClient;
import java.io.File;
import javax.net.ssl.SSLContext;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Creates a dedicated http client used by Feign when performing http calls to the Federation Gateway Service.
 */
@Component
public class CloudFederationFeignHttpClientProvider implements FederationFeignHttpClientProvider {

  private final Integer connectionPoolSize;
  private final File keyStore;
  private final String keyStorePassword;
  private final File trustStore;
  private final String trustStorePassword;

  private final HostnameVerifierProvider hostnameVerifierProvider;

  /**
   * Construct Provider.
   *
   * @param config .
   */
  public CloudFederationFeignHttpClientProvider(FederationGatewayConfig config,
      HostnameVerifierProvider hostnameVerifierProvider) {
    var ssl = config.getSsl();
    this.connectionPoolSize = config.getConnectionPoolSize();
    this.keyStore = ssl.getKeyStore();
    this.keyStorePassword = ssl.getKeyStorePassword();
    this.trustStore = ssl.getTrustStore();
    this.trustStorePassword = ssl.getTrustStorePassword();
    this.hostnameVerifierProvider = hostnameVerifierProvider;
  }

  /**
   * Creates a FeignClient.
   */
  @Override
  @Bean
  public Client createFeignClient() {
    return new ApacheHttpClient(
        federationHttpClientFactory(connectionPoolSize, keyStore, keyStorePassword).createBuilder().build());
  }

  /**
   * Creates an {@link ApacheHttpClientFactory} that validates SSL certificates but no host names.
   */
  private ApacheHttpClientFactory federationHttpClientFactory(int connectionPoolSize, File keyStorePath,
      String keyStorePass) {
    return new DefaultApacheHttpClientFactory(HttpClientBuilder.create()
        .setMaxConnPerRoute(connectionPoolSize)
        .setMaxConnTotal(connectionPoolSize)
        .setSSLContext(getSslContext(keyStorePath, keyStorePass))
        .setSSLHostnameVerifier(hostnameVerifierProvider.createHostnameVerifier()));
  }

  private SSLContext getSslContext(File keyStorePath, String keyStorePass) {
    try {
      return SSLContextBuilder.create().loadKeyMaterial(keyStorePath,
              emptyCharrArrayIfNull(keyStorePass),
              emptyCharrArrayIfNull(keyStorePass))
          .loadTrustMaterial(this.trustStore,
              emptyCharrArrayIfNull(this.trustStorePassword))
          .build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
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
