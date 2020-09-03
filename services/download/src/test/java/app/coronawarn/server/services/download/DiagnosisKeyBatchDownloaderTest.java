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

import static app.coronawarn.server.services.download.download.DiagnosisKeyBatchDownloader.EMPTY_HEADER;
import static app.coronawarn.server.services.download.download.DiagnosisKeyBatchDownloader.HEADER_BATCH_TAG;
import static app.coronawarn.server.services.download.download.DiagnosisKeyBatchDownloader.HEADER_NEXT_BATCH_TAG;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import app.coronawarn.server.common.federation.client.FederationFeignHttpClientProvider;
import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.federation.client.config.FederationGatewayConfig;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.download.download.DiagnosisKeyBatchDownloader;
import app.coronawarn.server.services.download.download.FederationGatewayResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import java.time.LocalDate;
import java.util.Optional;
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

  private static final LocalDate EXP_DATE = LocalDate.of(2020, 9, 1);
  private static final String EXP_BATCH_TAG = "507f191e810c19729de860ea";
  private static final ResponseDefinitionBuilder RESPONSE_NOT_FOUND = aResponse().withStatus(NOT_FOUND.value());
  private static final ResponseDefinitionBuilder RESPONSE_INVALID_PAYLOAD =
      aResponse().withStatus(OK.value()).withBody("somethingInvalid");

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
  void downloadFirstBatchReturnsEmptyIfNotFound() {
    server.stubFor(get(anyUrl()).willReturn(RESPONSE_NOT_FOUND));
    Optional<FederationGatewayResponse> actResponse = batchDownloader.downloadFirstBatch(EXP_DATE);
    assertThat(actResponse).isEmpty();
  }

  @Test
  void downloadBatchReturnsEmptyIfNotFound() {
    server.stubFor(get(anyUrl()).willReturn(RESPONSE_NOT_FOUND));
    Optional<FederationGatewayResponse> actResponse = batchDownloader.downloadBatch(EXP_DATE, EXP_BATCH_TAG);
    assertThat(actResponse).isEmpty();
  }

  @Test
  void downloadFirstBatchReturnsEmptyIfPayloadInvalid() {
    server.stubFor(get(anyUrl()).willReturn(RESPONSE_INVALID_PAYLOAD));
    Optional<FederationGatewayResponse> actResponse = batchDownloader.downloadFirstBatch(EXP_DATE);
    assertThat(actResponse).isEmpty();
  }

  @Test
  void downloadBatchReturnsEmptyIfPayloadInvalid() {
    server.stubFor(get(anyUrl()).willReturn(RESPONSE_INVALID_PAYLOAD));
    Optional<FederationGatewayResponse> actResponse = batchDownloader.downloadBatch(EXP_DATE, EXP_BATCH_TAG);
    assertThat(actResponse).isEmpty();
  }

  @Test
  void downloadFirstBatchReturnsResponseWithoutNextBatchTag() {
    server.stubFor(get(anyUrl()).willReturn(RESPONSE_NO_NEXT_BATCH));
    Optional<FederationGatewayResponse> actResponse = batchDownloader.downloadFirstBatch(EXP_DATE);
    assertThat(actResponse).contains(
        new FederationGatewayResponse(DiagnosisKeyBatch.newBuilder().build(), EXP_BATCH_TAG, Optional.empty()));
  }

  private static final ResponseDefinitionBuilder RESPONSE_NO_NEXT_BATCH = aResponse()
      .withStatus(OK.value())
      .withHeader(HEADER_BATCH_TAG, EXP_BATCH_TAG)
      .withHeader(HEADER_NEXT_BATCH_TAG, EMPTY_HEADER)
      .withBody(DiagnosisKeyBatch.newBuilder().build().toByteArray());

  private String buildValidPayload() {
    DiagnosisKeyBatch.newBuilder().build().toByteArray();
    return null;
  }
}
