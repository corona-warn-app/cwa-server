package app.coronawarn.server.common.federation.client;

import app.coronawarn.server.common.federation.client.config.FederationGatewayConfig;
import feign.Client;
import feign.httpclient.ApacheHttpClient;
import java.io.File;
import javax.net.ssl.SSLContext;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Creates a dedicated http client used by Feign when performing http calls to the Federation Gateway Service.
 */
@Component
@Profile("!disable-ssl-efgs-verification")
public class CloudFederationFeignHttpClientProvider implements FederationFeignHttpClientProvider {

  private final Integer connectionPoolSize;
  private final File keyStore;
  private final String keyStorePassword;
  private final HostnameVerifierProvider hostnameVerifierProvider;
  private final File trustStore;
  private final String trustStorePassword;

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
    this.keyStorePassword = ssl.getKeyStorePass();
    this.hostnameVerifierProvider = hostnameVerifierProvider;
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
        federationHttpClientFactory(connectionPoolSize, keyStore, keyStorePassword).createBuilder().build());
  }

  /**
   * Creates an {@link ApacheHttpClientFactory} that validates SSL certificates but no host names.
   */
  private ApacheHttpClientFactory federationHttpClientFactory(int connectionPoolSize, File keyStorePath,
      String keyStorePass) {
    return new DefaultApacheHttpClientFactory(HttpClientBuilder.create() //
        .setMaxConnPerRoute(connectionPoolSize) //
        .setMaxConnTotal(connectionPoolSize) //
        .setSSLContext(getSslContext(keyStorePath, keyStorePass))
        .setSSLHostnameVerifier(this.hostnameVerifierProvider.createHostnameVerifier()));
  }

  private SSLContext getSslContext(File keyStorePath, String keyStorePass) {
    try {
      return SSLContextBuilder
          .create()
          .loadKeyMaterial(keyStorePath, keyStorePass.toCharArray(), keyStorePass.toCharArray())
          .loadTrustMaterial(this.trustStore, this.trustStorePassword.toCharArray())
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
