package app.coronawarn.server.services.submission.verification;

import static java.util.concurrent.TimeUnit.SECONDS;

import app.coronawarn.server.common.federation.client.hostname.HostnameVerifierProvider;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig.Client.Ssl;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig.FeignRetry;
import feign.Client;
import feign.Retryer;
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
import org.springframework.stereotype.Component;

@Component
public class CloudFeignClientProvider {

  private final Integer connectionPoolSize;
  private final File keyStore;
  private final String keyStorePassword;
  private final String keyPassword;
  private final File trustStore;
  private final String trustStorePassword;

  private final FeignRetry retry;

  private final HostnameVerifierProvider hostnameVerifierProvider;

  /**
   * Creates a {@link CloudFeignClientProvider} that provides feign clients with fixed key and trust material.
   *
   * @param config                   config attributes of {@link SubmissionServiceConfig}
   * @param hostnameVerifierProvider provider {@link SubmissionServiceConfig}
   */
  public CloudFeignClientProvider(final SubmissionServiceConfig config,
      final HostnameVerifierProvider hostnameVerifierProvider) {
    final Ssl sslConfig = config.getClient().getSsl();
    keyStore = sslConfig.getKeyStore();
    keyStorePassword = sslConfig.getKeyStorePassword();
    keyPassword = sslConfig.getKeyPassword();
    trustStore = sslConfig.getTrustStore();
    trustStorePassword = sslConfig.getTrustStorePassword();
    connectionPoolSize = config.getConnectionPoolSize();
    this.hostnameVerifierProvider = hostnameVerifierProvider;
    retry = config.getFeignRetry();
  }

  @Bean
  public ApacheHttpClientConnectionManagerFactory createConnectionManager() {
    return new DefaultApacheHttpClientConnectionManagerFactory();
  }

  public Client createFeignClient() {
    return new ApacheHttpClient(createHttpClientFactory().createBuilder().build());
  }

  /**
   * Creates an {@link ApacheHttpClientFactory} that validates SSL certificates and host names.
   *
   * @return new ApacheHttpClientFactory
   */
  @Bean
  public ApacheHttpClientFactory createHttpClientFactory() {
    return new DefaultApacheHttpClientFactory(HttpClientBuilder.create()
        .setMaxConnPerRoute(connectionPoolSize)
        .setMaxConnTotal(connectionPoolSize)
        .setSSLContext(getSslContext())
        .setSSLHostnameVerifier(hostnameVerifierProvider.createHostnameVerifier()));
  }

  private SSLContext getSslContext() {
    try {
      return SSLContextBuilder
          .create()
          .loadKeyMaterial(keyStore, keyStorePassword.toCharArray(), keyPassword.toCharArray())
          .loadTrustMaterial(trustStore, trustStorePassword.toCharArray())
          .build();
    } catch (final IOException | GeneralSecurityException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates new {@link Retryer} with {@link #retry} values.
   * 
   * @return {@link Retryer} with {@link #retry} values.
   */
  @Bean
  public Retryer retryer() {
    return new Retryer.Default(retry.getPeriod(), SECONDS.toMillis(retry.getMaxPeriod()), retry.getMaxAttempts());
  }
}
