/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.common.federation.client;

import app.coronawarn.server.common.federation.client.config.FederationGatewayConfig;
import feign.Client;
import feign.httpclient.ApacheHttpClient;
import java.io.File;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
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
public class FederationFeignHttpClientProvider {

  private final Integer connectionPoolSize;
  private final File keyStore;
  private final String keyStorePassword;

  /**
   * Construct Provider.
   *
   * @param config .
   */
  public FederationFeignHttpClientProvider(FederationGatewayConfig config) {
    var ssl = config.getSsl();
    this.connectionPoolSize = config.getConnectionPoolSize();
    this.keyStore = ssl.getKeyStore();
    this.keyStorePassword = ssl.getKeyStorePass();
  }

  /**
   * Creates a FeignClient.
   */
  @Bean
  public Client createFeignClient() {
    return new ApacheHttpClient(
        federationHttpClientFactory(connectionPoolSize, keyStore, keyStorePassword)
            .createBuilder().build());
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
        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE));
  }

  private SSLContext getSslContext(File keyStorePath, String keyStorePass) {
    try {
      return SSLContextBuilder
          .create()
          .loadKeyMaterial(keyStorePath, keyStorePass.toCharArray(), keyStorePass.toCharArray())
          .loadTrustMaterial(TrustSelfSignedStrategy.INSTANCE)
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
