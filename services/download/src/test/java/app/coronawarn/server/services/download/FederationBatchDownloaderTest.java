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

import static app.coronawarn.server.services.download.download.FederationBatchDownloader.EMPTY_HEADER;
import static app.coronawarn.server.services.download.download.FederationBatchDownloader.HEADER_BATCH_TAG;
import static app.coronawarn.server.services.download.download.FederationBatchDownloader.HEADER_NEXT_BATCH_TAG;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import app.coronawarn.server.common.federation.client.FederationFeignHttpClientProvider;
import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.federation.client.config.FederationGatewayConfig;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.download.download.FederationBatchDownloader;
import app.coronawarn.server.services.download.download.FederationGatewayResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.google.protobuf.ByteString;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;

@ImportAutoConfiguration({FeignAutoConfiguration.class, FederationGatewayConfig.class, FeignClientConfiguration.class})
@SpringBootTest(classes = {FederationBatchDownloader.class, FederationGatewayClient.class,
    FederationFeignHttpClientProvider.class})
class FederationBatchDownloaderTest {

  private static final LocalDate EXP_DATE = LocalDate.of(2020, 9, 1);
  private static final String EXP_BATCH_TAG = "507f191e810c19729de860ea";
  private static final String EXP_NEXT_BATCH_TAG = "507f191e810c19729de860ea";
  private static final DiagnosisKeyBatch EXP_DIAGNOSIS_KEY_BATCH = DiagnosisKeyBatch.newBuilder()
      .addKeys(
          DiagnosisKey.newBuilder()
              .setKeyData(ByteString.copyFromUtf8("0123456789ABCDEF"))
              .addVisitedCountries("DE")
              .setRollingStartIntervalNumber(0)
              .setRollingPeriod(144)
              .setTransmissionRiskLevel(2)
              .build()).build();

  private static WireMockServer server;

  @Autowired
  private FederationBatchDownloader batchDownloader;

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

  private static final ResponseDefinitionBuilder RESPONSE_NOT_FOUND = aServerResponse().withStatus(NOT_FOUND.value());

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

  private static final ResponseDefinitionBuilder RESPONSE_INVALID_PAYLOAD =
      aServerResponse().withStatus(OK.value()).withBody("somethingInvalid");

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

  private static final ResponseDefinitionBuilder RESPONSE_NO_NEXT_BATCH = aServerResponse()
      .withStatus(OK.value())
      .withHeader(HEADER_BATCH_TAG, EXP_BATCH_TAG)
      .withHeader(HEADER_NEXT_BATCH_TAG, EMPTY_HEADER)
      .withBody(EXP_DIAGNOSIS_KEY_BATCH.toByteArray());

  @Test
  void downloadFirstBatchReturnsResponseWithoutNextBatchTag() {
    server.stubFor(get(anyUrl()).willReturn(RESPONSE_NO_NEXT_BATCH));
    Optional<FederationGatewayResponse> actResponse = batchDownloader.downloadFirstBatch(EXP_DATE);
    assertThat(actResponse).contains(
        new FederationGatewayResponse(EXP_DIAGNOSIS_KEY_BATCH, EXP_BATCH_TAG, Optional.empty()));
  }

  @Test
  void downloadBatchReturnsResponseWithoutNextBatchTag() {
    server.stubFor(get(anyUrl()).willReturn(RESPONSE_NO_NEXT_BATCH));
    Optional<FederationGatewayResponse> actResponse = batchDownloader.downloadBatch(EXP_DATE, EXP_BATCH_TAG);
    assertThat(actResponse).contains(
        new FederationGatewayResponse(EXP_DIAGNOSIS_KEY_BATCH, EXP_BATCH_TAG, Optional.empty()));
  }

  private static final ResponseDefinitionBuilder RESPONSE_NEXT_BATCH_PRESENT = aServerResponse()
      .withStatus(OK.value())
      .withHeader(HEADER_BATCH_TAG, EXP_BATCH_TAG)
      .withHeader(HEADER_NEXT_BATCH_TAG, EXP_NEXT_BATCH_TAG)
      .withBody(EXP_DIAGNOSIS_KEY_BATCH.toByteArray());

  @Test
  void downloadFirstBatchReturnsResponseWithNextBatchTag() {
    server.stubFor(get(anyUrl()).willReturn(RESPONSE_NEXT_BATCH_PRESENT));
    Optional<FederationGatewayResponse> actResponse = batchDownloader.downloadFirstBatch(EXP_DATE);
    assertThat(actResponse).contains(
        new FederationGatewayResponse(EXP_DIAGNOSIS_KEY_BATCH, EXP_BATCH_TAG, Optional.of(EXP_NEXT_BATCH_TAG)));
  }

  @Test
  void downloadBatchReturnsResponseWithNextBatchTag() {
    server.stubFor(get(anyUrl()).willReturn(RESPONSE_NEXT_BATCH_PRESENT));
    Optional<FederationGatewayResponse> actResponse = batchDownloader.downloadBatch(EXP_DATE, EXP_BATCH_TAG);
    assertThat(actResponse).contains(
        new FederationGatewayResponse(EXP_DIAGNOSIS_KEY_BATCH, EXP_BATCH_TAG, Optional.of(EXP_NEXT_BATCH_TAG)));
  }

  private static ResponseDefinitionBuilder aServerResponse() {
    return aResponse().withHeader(CONTENT_TYPE, "application/protobuf; version=1.0");
  }
}
