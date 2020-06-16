/*
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.submission.verification;

import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig.Client.Ssl;
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
@Profile("!disable-ssl-client-verification")
public class CloudFeignClientProvider implements FeignClientProvider {

  private final HostnameVerifierProvider hostnameVerifierProvider;
  private final Integer connectionPoolSize;
  private final File keyStore;
  private final String keyStorePassword;
  private final String keyPassword;
  private final File trustStore;
  private final String trustStorePassword;

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

  @Override
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
        .setSSLHostnameVerifier(this.hostnameVerifierProvider.createHostnameVerifier()));
  }

  @Bean
  public ApacheHttpClientConnectionManagerFactory createConnectionManager() {
    return new DefaultApacheHttpClientConnectionManagerFactory();
  }
}
