package app.coronawarn.server.common.federation.client;

import static app.coronawarn.server.common.shared.util.CwaStringUtils.emptyCharrArrayIfNull;

import app.coronawarn.server.common.federation.client.hostname.HostnameVerifierProvider;
import feign.Client;
import feign.hc5.ApacheHttp5Client;
import java.io.File;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.socket.LayeredConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.cloud.openfeign.support.FeignHttpClientProperties;
import org.springframework.context.annotation.Bean;

/**
 * Creates a dedicated http client used by Feign when performing http calls.
 */
public abstract class AbstractFeignClientProvider {

  private final int connectionPoolSize;
  private final HostnameVerifierProvider hostnameVerifierProvider;
  private final File keyStore;
  private final String keyStorePassword;
  private final String keyPassword;
  private final File trustStore;
  private final String trustStorePassword;

  public AbstractFeignClientProvider(final int connectionPoolSize, final File trustStore,
      final String trustStorePassword) {
    this(connectionPoolSize, trustStore, trustStorePassword, null, null, null, null);
  }

  /**
   * AbstractFeignClientProvider.
   *
   * @param connectionPoolSize       connection pool size
   * @param trustStore               trust store
   * @param trustStorePassword       trust store password
   * @param keyStore                 key store
   * @param keyStorePassword         key store password
   * @param hostnameVerifierProvider hostname verifier provider
   */
  public AbstractFeignClientProvider(final int connectionPoolSize, final File trustStore,
      final String trustStorePassword, final File keyStore,
      final String keyStorePassword, final String keyPassword,
      final HostnameVerifierProvider hostnameVerifierProvider) {
    this.connectionPoolSize = connectionPoolSize;
    this.trustStore = trustStore;
    this.trustStorePassword = trustStorePassword;
    this.keyStore = keyStore;
    this.keyStorePassword = keyStorePassword;
    this.keyPassword = keyPassword;
    this.hostnameVerifierProvider = hostnameVerifierProvider;
  }

  /**
   * Creates a FeignClient.
   */
  @Bean
  public Client createFeignClient() {
    final FeignHttpClientProperties httpClientProperties = new FeignHttpClientProperties();
    return new ApacheHttp5Client(HttpClients.custom().disableCookieManagement().useSystemProperties()
        .setConnectionManager(hc5ConnectionManager(httpClientProperties)).evictExpiredConnections()
        .setDefaultRequestConfig(RequestConfig.custom()
            .setConnectTimeout(
                Timeout.of(httpClientProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS))
            .setRedirectsEnabled(httpClientProperties.isFollowRedirects())
            .setConnectionRequestTimeout(
                Timeout.of(httpClientProperties.getHc5().getConnectionRequestTimeout(),
                    httpClientProperties.getHc5().getConnectionRequestTimeoutUnit()))
            .build())
        .build());
  }

  public int getConnectionPoolSize() {
    return connectionPoolSize;
  }

  public HostnameVerifier getHostnameVerifier() {
    return hostnameVerifierProvider == null ? null : hostnameVerifierProvider.createHostnameVerifier();
  }

  public String getKeyPassword() {
    return keyPassword == null ? keyStorePassword : keyPassword;
  }

  public File getKeyStore() {
    return keyStore;
  }

  public String getKeyStorePassword() {
    return keyStorePassword;
  }

  protected SSLContext getSslContext(final File keyStorePath, final String storePass, final String keyPass) {
    try {
      final SSLContextBuilder builder = SSLContextBuilder.create();
      if (keyStorePath != null && keyStorePath.canRead()) {
        builder.loadKeyMaterial(keyStorePath, emptyCharrArrayIfNull(storePass), emptyCharrArrayIfNull(keyPass));
      }
      return builder.loadTrustMaterial(getTrustStore(), emptyCharrArrayIfNull(getTrustStorePassword())).build();
    } catch (final Exception e) {
      throw new CloudFeignHttpClientProviderException(e);
    }
  }

  public File getTrustStore() {
    return trustStore;
  }

  public String getTrustStorePassword() {
    return trustStorePassword;
  }

  /**
   * Creates HttpClientConnectionManager.
   *
   * @param httpClientProperties other client properties.
   * @return new HttpClientConnectionManager
   */
  @Bean
  public HttpClientConnectionManager hc5ConnectionManager(final FeignHttpClientProperties httpClientProperties) {
    return PoolingHttpClientConnectionManagerBuilder.create()
        .setSSLSocketFactory(sslConnectionSocketFactory())
        .setMaxConnTotal(getConnectionPoolSize())
        .setMaxConnPerRoute(getConnectionPoolSize())
        .setConnPoolPolicy(PoolReusePolicy.valueOf(httpClientProperties.getHc5().getPoolReusePolicy().name()))
        .setPoolConcurrencyPolicy(
            PoolConcurrencyPolicy.valueOf(httpClientProperties.getHc5().getPoolConcurrencyPolicy().name()))
        .setConnectionTimeToLive(
            TimeValue.of(httpClientProperties.getTimeToLive(), httpClientProperties.getTimeToLiveUnit()))
        .setDefaultSocketConfig(
            SocketConfig.custom().setSoTimeout(Timeout.of(httpClientProperties.getHc5().getSocketTimeout(),
                httpClientProperties.getHc5().getSocketTimeoutUnit())).build())
        .build();
  }

  protected LayeredConnectionSocketFactory sslConnectionSocketFactory() {
    final SSLConnectionSocketFactoryBuilder builder = SSLConnectionSocketFactoryBuilder.create()
        .setTlsVersions(TLS.V_1_3, TLS.V_1_2);
    builder.setSslContext(getSslContext(getKeyStore(), getKeyStorePassword(), getKeyPassword()));
    builder.setHostnameVerifier(getHostnameVerifier());
    return builder.build();
  }
}
