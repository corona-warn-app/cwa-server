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

/**
 * Creates a dedicated http client used by Feign when performing http calls to the Federation Gateway Service.
 */
public class FederationFeignHttpClientProvider {

  /**
   * Creates a FeignClient.
   */
  public Client createFeignClient(int connectionPoolSize, String keyStorePath, String keyStorePass,
                                  String certificateType) {
    return new ApacheHttpClient(
        federationHttpClientFactory(connectionPoolSize, keyStorePath, keyStorePass, certificateType)
            .createBuilder().build());
  }

  /**
   * Creates an {@link ApacheHttpClientFactory} that validates SSL certificates but no host names.
   */
  private ApacheHttpClientFactory federationHttpClientFactory(int connectionPoolSize, String keyStorePath,
                                                              String keyStorePass,
                                                              String certificateType) {
    return new DefaultApacheHttpClientFactory(HttpClientBuilder.create()
        .setMaxConnPerRoute(connectionPoolSize)
        .setMaxConnTotal(connectionPoolSize)
        .setSSLContext(getSslContext(keyStorePath, keyStorePass, certificateType))
        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE));
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
