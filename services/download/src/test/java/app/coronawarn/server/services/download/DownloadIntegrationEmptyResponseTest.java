

package app.coronawarn.server.services.download;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.persistence.repository.FederationBatchInfoRepository;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.download.config.DownloadServiceConfig;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * This integration test is responsible for testing the runners for download and retention policy.
 * <p>
 * The WireMockServer will return a series of three batches, where the first batch of the corresponding date is batch1,
 * that can be processed successfully. Batch2 is returned by an explicit call to its batch tag, but the response is
 * empty (like it is the case on EFGS). Its successor batch3 fails with a 404 Not Found.
 * <p>
 * Hence, after the execution of both runners, the federation_batch_info table should be the following:
 * <li>"batch1_tag" has state "PROCESSED"</li>
 * <li>"batch2_tag" has state "ERROR"</li>
 * <li>"batch3_tag" has state "ERROR" * no batch has state "UNPROCESSED"</li>
 * <p>
 * The diagnosis_key table should contain the data of batch1.
 */
@SpringBootTest
@DirtiesContext
@ActiveProfiles("download-empty-response-integration-test")
class DownloadIntegrationEmptyResponseTest {

  public static final String BATCH1_DATA = "0123456789ABCDED";

  private static final String BATCH1_TAG = "batch1_tag";
  private static final String BATCH2_TAG = "batch2_tag";
  private static final String BATCH3_TAG = "batch3_tag";

  private static final String EMPTY_BATCH_TAG = "null";

  private static WireMockServer server;

  @Autowired
  private FederationBatchInfoRepository federationBatchInfoRepository;

  @Autowired
  private DiagnosisKeyRepository diagnosisKeyRepository;

  @Autowired
  private DownloadServiceConfig downloadServiceConfig;

  @BeforeAll
  static void setupWireMock() {
    HttpHeaders batch1Headers = getHttpHeaders(BATCH1_TAG, BATCH2_TAG);
    DiagnosisKeyBatch batch1 = FederationBatchTestHelper.createDiagnosisKeyBatch(BATCH1_DATA);

    HttpHeaders batch2Headers = getHttpHeaders(BATCH2_TAG, BATCH3_TAG);

    HttpHeaders batch3Headers = getHttpHeaders(BATCH3_TAG, EMPTY_BATCH_TAG);

    server = new WireMockServer(options().port(1234));
    server.start();
    server.stubFor(
        get(anyUrl())
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeaders(batch1Headers)
                    .withBody(batch1.toByteArray())));
    server.stubFor(
        get(anyUrl())
            .withHeader("batchTag", equalTo(BATCH2_TAG))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeaders(batch2Headers)));
    server.stubFor(
        get(anyUrl())
            .withHeader("batchTag", equalTo(BATCH3_TAG))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.NOT_FOUND.value())
                    .withHeaders(batch3Headers)));
  }

  private static HttpHeaders getHttpHeaders(String batchTag, String nextBatchTag) {
    return new HttpHeaders()
        .plus(new HttpHeader(CONTENT_TYPE, "application/protobuf; version=1.0"))
        .plus(new HttpHeader("batchTag", batchTag))
        .plus(new HttpHeader("nextBatchTag", nextBatchTag));
  }

  @AfterAll
  static void tearDown() {
    server.stop();
  }

  @Test
  void testDownloadRunSuccessfully() {
    assertThat(federationBatchInfoRepository.findAll()).hasSize(3);
    assertThat(federationBatchInfoRepository.findByStatus("UNPROCESSED")).isEmpty();
    assertThat(federationBatchInfoRepository.findByStatus("PROCESSED")).hasSize(2);
    assertThat(federationBatchInfoRepository.findByStatus("ERROR")).hasSize(1);

    Iterable<DiagnosisKey> diagnosisKeys = diagnosisKeyRepository.findAll();
    assertThat(diagnosisKeys)
        .hasSize(1)
        .contains(FederationBatchTestHelper.createDiagnosisKey(BATCH1_DATA, downloadServiceConfig));
  }
}
