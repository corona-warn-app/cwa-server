

package app.coronawarn.server.services.submission.verification;

import app.coronawarn.server.common.federation.client.HostnameVerifierProvider;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig.Client.Ssl;
import feign.Client;
import feign.httpclient.ApacheHttpClient;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class CloudFeignClientProvider {

  private final Integer connectionPoolSize;
  private final File keyStore;
  private final String keyStorePassword;
  private final String keyPassword;
  private final File trustStore;
  private final String trustStorePassword;

  private final HostnameVerifierProvider hostnameVerifierProvider;

  /**
   * Creates a {@link CloudFeignClientProvider} that provides feign clients with fixed key and trust material.
   */
  public CloudFeignClientProvider(SubmissionServiceConfig config, HostnameVerifierProvider hostnameVerifierProvider) {
    Ssl sslConfig = config.getClient().getSsl();
    this.keyStore = sslConfig.getKeyStore();
    this.keyStorePassword = sslConfig.getKeyStorePassword();
    this.keyPassword = sslConfig.getKeyPassword();
    this.trustStore = sslConfig.getTrustStore();
    this.trustStorePassword = sslConfig.getTrustStorePassword();
    this.connectionPoolSize = config.getConnectionPoolSize();
    this.hostnameVerifierProvider = hostnameVerifierProvider;
  }

  public Client createFeignClient() {
    return new ApacheHttpClient(createHttpClientFactory().createBuilder().build());
  }

  private SSLContext getSslContext() {
    try {
      return SSLContextBuilder
          .create()
          .loadKeyMaterial(this.keyStore, this.keyStorePassword.toCharArray(), this.keyPassword.toCharArray())
          .loadTrustMaterial(this.trustStore, this.trustStorePassword.toCharArray())
          .build();
    } catch (IOException | GeneralSecurityException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates an {@link ApacheHttpClientFactory} that validates SSL certificates and host names.
   */
  @Bean
  public ApacheHttpClientFactory createHttpClientFactory() {
    return new DefaultApacheHttpClientFactory(HttpClientBuilder.create()
        .setMaxConnPerRoute(this.connectionPoolSize)
        .setMaxConnTotal(this.connectionPoolSize)
        .setSSLContext(getSslContext())
        .setSSLHostnameVerifier(hostnameVerifierProvider.createHostnameVerifier()));
  }

  @Bean
  public ApacheHttpClientConnectionManagerFactory createConnectionManager() {
    return new DefaultApacheHttpClientConnectionManagerFactory();
  }
}
