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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext
class FederationGatewayDownloadServiceTest {

  private static final WireMockServer server = new WireMockServer(1234);

  @Autowired
  private FederationGatewayDownloadService downloadService;

  @BeforeEach
  void ensureRunningServer() {
    if (!server.isRunning()) {
      server.start();
    }
  }

  @AfterEach
  void resetServer() {
    server.resetAll();
  }

  @AfterAll
  static void stopServer() {
    server.stop();
  }

  @Test
  void test404IsCaught() {
    server.stubFor(
        get(anyUrl())
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.NOT_FOUND.value())));

    assertThatExceptionOfType(FederationGatewayException.class)
        .isThrownBy(() -> downloadService.downloadBatch(mock(LocalDate.class)));
    assertThatExceptionOfType(FederationGatewayException.class)
        .isThrownBy(() -> downloadService.downloadBatch("batchTag", mock(LocalDate.class)));
  }

  @Test
  void testMissingBatchTagInResponseHeader() {
    server.stubFor(
        get(anyUrl())
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())));

    assertThatExceptionOfType(FederationGatewayException.class)
        .isThrownBy(() -> downloadService.downloadBatch(mock(LocalDate.class)));
    assertThatExceptionOfType(FederationGatewayException.class)
        .isThrownBy(() -> downloadService.downloadBatch("batchTag", mock(LocalDate.class)));
  }

  @Test
  void testNextBatchTagIsParsedWithEmptyResponseBody() {
    String batchTag = "batch-tag";
    String nextBatchTag = "next-batch-tag";

    server.stubFor(
        get(anyUrl())
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", "application/protobuf; version=1.0")
                    .withHeader("batchTag", batchTag)
                    .withHeader("nextBatchTag", nextBatchTag)));

    LocalDate date = mock(LocalDate.class);
    BatchDownloadResponse expResponse = new BatchDownloadResponse(batchTag, Optional.empty(),
        Optional.of(nextBatchTag));
    assertThat(downloadService.downloadBatch(date)).isEqualTo(expResponse);
    assertThat(downloadService.downloadBatch(batchTag, date)).isEqualTo(expResponse);
  }

  @Test
  void testDownloadSuccessful() {
    String batchTag = "batch-tag";
    String nextBatchTag = "next-batch-tag";
    DiagnosisKeyBatch batch = FederationBatchTestHelper.createDiagnosisKeyBatch("batch-data");

    server.stubFor(
        get(anyUrl())
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", "application/protobuf; version=1.0")
                    .withHeader("batchTag", batchTag)
                    .withHeader("nextBatchTag", nextBatchTag)
                    .withBody(batch.toByteArray())));

    LocalDate date = mock(LocalDate.class);
    BatchDownloadResponse expResponse = new BatchDownloadResponse(batchTag, Optional.of(batch),
        Optional.of(nextBatchTag));
    assertThat(downloadService.downloadBatch(date)).isEqualTo(expResponse);
    assertThat(downloadService.downloadBatch(batchTag, date)).isEqualTo(expResponse);
  }

  @Test
  void testDownloadSuccessfulWithoutNextBatchTag() {
    String batchTag = "batch-tag";
    DiagnosisKeyBatch batch = FederationBatchTestHelper.createDiagnosisKeyBatch("batch-data");

    server.stubFor(
        get(anyUrl())
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", "application/protobuf; version=1.0")
                    .withHeader("batchTag", batchTag)
                    .withBody(batch.toByteArray())));

    LocalDate date = mock(LocalDate.class);
    BatchDownloadResponse expResponse = new BatchDownloadResponse(batchTag, Optional.of(batch), Optional.empty());
    assertThat(downloadService.downloadBatch(date)).isEqualTo(expResponse);
    assertThat(downloadService.downloadBatch(batchTag, date)).isEqualTo(expResponse);
  }
}
