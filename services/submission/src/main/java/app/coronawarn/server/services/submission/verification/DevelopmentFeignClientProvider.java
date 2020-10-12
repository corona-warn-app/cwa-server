

package app.coronawarn.server.services.submission.verification;

import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import feign.Client;
import feign.httpclient.ApacheHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("disable-ssl-client-verification")
public class DevelopmentFeignClientProvider implements FeignClientProvider {

  private final HostnameVerifierProvider hostnameVerifierProvider;
  private final Integer connectionPoolSize;

  public DevelopmentFeignClientProvider(SubmissionServiceConfig config,
      HostnameVerifierProvider hostnameVerifierProvider) {
    this.connectionPoolSize = config.getConnectionPoolSize();
    this.hostnameVerifierProvider = hostnameVerifierProvider;
  }

  @Override
  public Client createFeignClient() {
    return new ApacheHttpClient(createHttpClientFactory().createBuilder().build());
  }

  /**
   * Creates an {@link ApacheHttpClientFactory} that neither validates SSL certificates nor host names.
   */
  @Bean
  public ApacheHttpClientFactory createHttpClientFactory() {
    return new DefaultApacheHttpClientFactory(HttpClientBuilder.create()
        .setMaxConnPerRoute(this.connectionPoolSize)
        .setMaxConnTotal(this.connectionPoolSize)
        .setSSLHostnameVerifier(this.hostnameVerifierProvider.createHostnameVerifier()));
  }

  @Bean
  public ApacheHttpClientConnectionManagerFactory createConnectionManager() {
    return new DefaultApacheHttpClientConnectionManagerFactory();
  }
}
