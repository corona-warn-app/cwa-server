

package app.coronawarn.server.services.download;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

  private static final WireMockServer SERVER = new WireMockServer(1234);
  private static final String BATCH_TAG = "batch-tag";
  private static final String NEXT_BATCH_TAG = "next-batch-tag";
  private static final LocalDate DATE = mock(LocalDate.class);

  @Autowired
  private FederationGatewayDownloadService downloadService;

  @BeforeEach
  void ensureRunningServer() {
    if (!SERVER.isRunning()) {
      SERVER.start();
    }
  }

  @AfterEach
  void resetServer() {
    SERVER.resetAll();
  }

  @AfterAll
  static void stopServer() {
    SERVER.stop();
  }

  @Test
  void test404IsCaught() {
    SERVER.stubFor(
        get(anyUrl())
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.NOT_FOUND.value())));

    assertExceptionIsThrown();
  }

  @Test
  void testMissingBatchTagInResponseHeader() {
    SERVER.stubFor(
        get(anyUrl())
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())));

    assertExceptionIsThrown();
  }

  @Test
  void testNextBatchTagIsParsedWithEmptyResponseBody() throws Exception {

    SERVER.stubFor(
        get(anyUrl())
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", "application/protobuf; version=1.0")
                    .withHeader("batchTag", BATCH_TAG)
                    .withHeader("nextBatchTag", NEXT_BATCH_TAG)));

    BatchDownloadResponse expResponse = new BatchDownloadResponse(BATCH_TAG, Optional.empty(),
        Optional.of(NEXT_BATCH_TAG));
    assertDownloadResponseMatches(expResponse);
  }

  @Test
  void testDownloadSuccessful() throws Exception {
    DiagnosisKeyBatch batch = FederationBatchTestHelper.createDiagnosisKeyBatch("batch-data");

    SERVER.stubFor(
        get(anyUrl())
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", "application/protobuf; version=1.0")
                    .withHeader("batchTag", BATCH_TAG)
                    .withHeader("nextBatchTag", NEXT_BATCH_TAG)
                    .withBody(batch.toByteArray())));

    BatchDownloadResponse expResponse = new BatchDownloadResponse(BATCH_TAG, Optional.of(batch),
        Optional.of(NEXT_BATCH_TAG));
    assertDownloadResponseMatches(expResponse);
  }

  @Test
  void testDownloadSuccessfulWithoutNextBatchTag() throws Exception {
    DiagnosisKeyBatch batch = FederationBatchTestHelper.createDiagnosisKeyBatch("batch-data");

    SERVER.stubFor(
        get(anyUrl())
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", "application/protobuf; version=1.0")
                    .withHeader("batchTag", BATCH_TAG)
                    .withBody(batch.toByteArray())));

    BatchDownloadResponse expResponse = new BatchDownloadResponse(BATCH_TAG, Optional.of(batch), Optional.empty());
    assertDownloadResponseMatches(expResponse);
  }


  @Test
  void testDownloadBatchNotAuthenticated() {
    SERVER.stubFor(
        get(anyUrl())
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.FORBIDDEN.value())));

    assertThatThrownBy(() -> downloadService.downloadBatch(BATCH_TAG, DATE))
        .isExactlyInstanceOf(FatalFederationGatewayException.class);
    assertThatThrownBy(() -> downloadService.downloadBatch(DATE))
        .isExactlyInstanceOf(FatalFederationGatewayException.class);
  }

  void assertDownloadResponseMatches(BatchDownloadResponse expResponse) throws Exception {
    assertThat(downloadService.downloadBatch(DATE)).isEqualTo(expResponse);
    assertThat(downloadService.downloadBatch(BATCH_TAG, DATE)).isEqualTo(expResponse);
  }

  void assertExceptionIsThrown() {
    assertThatExceptionOfType(BatchDownloadException.class)
        .isThrownBy(() -> downloadService.downloadBatch(DATE));
    assertThatExceptionOfType(BatchDownloadException.class)
        .isThrownBy(() -> downloadService.downloadBatch(BATCH_TAG, DATE));
  }
}
