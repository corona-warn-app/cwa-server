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

package app.coronawarn.server.services.download;

import app.coronawarn.server.common.federation.client.config.FederationGatewayConfig;
import app.coronawarn.server.common.federation.client.download.FederationGatewayHttpMessageConverter;
import feign.Client;
import feign.httpclient.ApacheHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientFactory;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@EnableFeignClients("app.coronawarn.server.common.federation.client")
public class FeignClientConfiguration {

  @Bean
  public HttpMessageConverters httpMessageConverters() {
    return new HttpMessageConverters(new FederationGatewayHttpMessageConverter());
  }

  @Bean
  Client feignDedicatedHttpClient(FederationGatewayConfig federationGatewayConfig) {
    return new ApacheHttpClient(createHttpClientFactory().createBuilder().build());
  }

  @Bean
  public ApacheHttpClientFactory createHttpClientFactory() {
    return new DefaultApacheHttpClientFactory(HttpClientBuilder.create()
        .setMaxConnPerRoute(1)
        .setMaxConnTotal(1));
  }

  @Bean
  public ApacheHttpClientConnectionManagerFactory createConnectionManager() {
    return new DefaultApacheHttpClientConnectionManagerFactory();
  }
}
