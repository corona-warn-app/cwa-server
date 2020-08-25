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

public class FederationFeignClientProvider {

  public Client createFeignClient(String keyStorePath, String keyStorePass, String certificateType) {
    return new ApacheHttpClient(
        federationHttpClientFactory(keyStorePath, keyStorePass, certificateType).createBuilder().build());
  }

  /**
   * Creates an {@link ApacheHttpClientFactory} that validates SSL certificates but no host names.
   */
  private ApacheHttpClientFactory federationHttpClientFactory(String keyStorePath, String keyStorePass,
      String certificateType) {
    return new DefaultApacheHttpClientFactory(HttpClientBuilder.create()
        .setMaxConnPerRoute(10)
        .setMaxConnTotal(10)
        .setSSLContext(getSslContext(keyStorePath, keyStorePass, certificateType))
        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE));//TODO:: investigate if verify host name is necessary
  }

  private SSLContext getSslContext(String keyStorePath, String keyStorePass, String certificateType) {
    try {
      return SSLContextBuilder
          .create()
          .loadKeyMaterial(readStore(keyStorePath, keyStorePass, certificateType), keyStorePass.toCharArray())
          .loadTrustMaterial(TrustSelfSignedStrategy.INSTANCE)
          .build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private KeyStore readStore(String keyStorePath, String keyStorePass, String certificateType) throws Exception {
    try (InputStream keyStoreStream = this.getClass().getResourceAsStream(keyStorePath)) {
      KeyStore keyStore = KeyStore.getInstance(certificateType);
      keyStore.load(keyStoreStream, keyStorePass.toCharArray());
      return keyStore;
    }
  }
}
