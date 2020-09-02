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

package app.coronawarn.server.services.download;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import app.coronawarn.server.common.federation.client.FederationFeignHttpClientProvider;
import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.federation.client.config.FederationGatewayConfig;
import app.coronawarn.server.services.download.download.DiagnosisKeyBatchDownloader;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;

@EnableFeignClients
@ImportAutoConfiguration({FeignAutoConfiguration.class, FederationGatewayConfig.class, FeignClientConfiguration.class})
@SpringBootTest(classes = {DiagnosisKeyBatchDownloader.class, FederationGatewayClient.class,
    FederationFeignHttpClientProvider.class})
@ActiveProfiles({"feign"})
class DiagnosisKeyBatchDownloaderTest {

  LocalDate EXP_DATE = LocalDate.of(2020, 9, 1);
  private static WireMockServer server;

  @Autowired
  private DiagnosisKeyBatchDownloader batchDownloader;

  @BeforeAll
  static void setupWireMock() {
    server = new WireMockServer(options().port(1234));
    server.start();
  }

  @BeforeEach
  void setup() {
    server.resetAll();
  }

  @AfterAll
  static void tearDown() {
    server.stop();
  }

  @Test
  void downloadBatchForDateReturnsEmptyIfNotFound() {
    server.stubFor(get(anyUrl()).willReturn(aResponse().withStatus(NOT_FOUND.value())));
    var actResponse = batchDownloader.downloadBatch(EXP_DATE);
    Assertions.assertThat(actResponse).isEmpty();
  }
}
