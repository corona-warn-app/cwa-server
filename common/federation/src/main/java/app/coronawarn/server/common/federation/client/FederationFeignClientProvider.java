package app.coronawarn.server.common.federation.client;

import feign.Client;
import feign.httpclient.ApacheHttpClient;
import java.io.InputStream;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class FederationFeignClientProvider implements FeignClientProvider {


  //TODO:: extenalize configuration in application.yaml to point to vault
  private static final String KEYSTOREPATH = "<keystore-path>";
  private static final String KEYSTOREPASS = "<keystore-pass>";
  private static final String KEYPASS = "<key-pass>";
  private static final String CERTIFICATE_TYPE = "PKCS12";

  @Override
  public Client createFeignClient() {
    return new ApacheHttpClient(federationHttpClientFactory().createBuilder().build());
  }

  /**
   * Creates an {@link ApacheHttpClientFactory} that validates SSL certificates but no host names.
   */
  @Bean
  public ApacheHttpClientFactory federationHttpClientFactory() {
    return new DefaultApacheHttpClientFactory(HttpClientBuilder.create()
        .setMaxConnPerRoute(10)
        .setMaxConnTotal(10)
        .setSSLContext(getSslContext())
        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE));//TODO:: investigate if verify host name is necessary
  }

  private SSLContext getSslContext() {
    try {
      return SSLContextBuilder
          .create()
          .loadKeyMaterial(readStore(), KEYPASS.toCharArray())
          .loadTrustMaterial(TrustSelfSignedStrategy.INSTANCE)
          .build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private KeyStore readStore() throws Exception {
    try (InputStream keyStoreStream = this.getClass().getResourceAsStream(KEYSTOREPATH)) {
      KeyStore keyStore = KeyStore.getInstance(CERTIFICATE_TYPE);
      keyStore.load(keyStoreStream, KEYSTOREPASS.toCharArray());
      return keyStore;
    }
  }
}
