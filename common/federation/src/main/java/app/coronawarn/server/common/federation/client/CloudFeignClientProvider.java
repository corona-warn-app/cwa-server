

package app.coronawarn.server.common.federation.client;

import app.coronawarn.server.common.federation.client.config.FederationGatewayConfig;
import app.coronawarn.server.common.federation.client.config.FederationGatewayConfig.Ssl;
import feign.Client;
import feign.httpclient.ApacheHttpClient;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLContext;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!disable-ssl-client-federation")
public class CloudFeignClientProvider implements FeignClientProvider {

  private final HostnameVerifierProvider hostnameVerifierProvider;
  private final Integer connectionPoolSize;
  private final File keyStore;
  private final String keyStorePassword;
  private final File keyTruststore;
  private final String keyTruststorePassword;


  /**
   * Creates a {@link CloudFeignClientProvider} that provides feign clients with fixed key and trust material.
   */
  public CloudFeignClientProvider(FederationGatewayConfig config, HostnameVerifierProvider hostnameVerifierProvider) {
    Ssl sslConfig = config.getSsl();
    this.keyStore = sslConfig.getKeyStore();
    this.keyStorePassword = sslConfig.getKeyStorePass();
    this.connectionPoolSize = config.getConnectionPoolSize();
    this.hostnameVerifierProvider = hostnameVerifierProvider;
    this.keyTruststore = sslConfig.getTrustStore();
    this.keyTruststorePassword = sslConfig.getTrustStorePassword();
  }

  @Override
  public Client createFeignClient() {
    return new ApacheHttpClient(federationHttpClientFactory()
        .createBuilder().build());
  }

  private SSLContext getSslContext() {
    try {
      return SSLContextBuilder
          .create()
          .loadKeyMaterial(this.keyStore, this.keyStorePassword.toCharArray(), this.keyStorePassword.toCharArray())
          .loadTrustMaterial(this.keyTruststore, this.keyTruststorePassword.toCharArray())
          .build();
    } catch (IOException | GeneralSecurityException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates an {@link ApacheHttpClientFactory} that validates SSL certificates but no host names.
   */
  private ApacheHttpClientFactory federationHttpClientFactory() {
    return new DefaultApacheHttpClientFactory(HttpClientBuilder.create()
        .setMaxConnPerRoute(connectionPoolSize)
        .setMaxConnTotal(connectionPoolSize)
        .setSSLContext(getSslContext())
        .setSSLHostnameVerifier(this.hostnameVerifierProvider.createHostnameVerifier()));
  }

  @Bean
  public ApacheHttpClientConnectionManagerFactory createConnectionManager() {
    return new DefaultApacheHttpClientConnectionManagerFactory();
  }
}
