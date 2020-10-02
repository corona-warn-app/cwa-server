

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
 * This integration test is responsible for testing the runners for download and retention policy. The Spring profile
 * "federation-download-integration" enables the test data generation in /db/testdata/V99__createTestDataForIntegrationTest.sql
 * via the application-federation-download-integration.yaml.
 * <p>
 * The sql script for the test data contains
 * <li>a batch info for an expired batch that should be deleted by the retention policy</li>
 * <li>and two batch info from the current date of status 'ERROR', which should be reprocessed.</li>
 * One of them will be successfully reprocessed and the other one will fail. The WireMockServer is configured
 * accordingly.
 * <p>
 * The WireMockServer will additionally return a series of three batches, where the first batch of the corresponding
 * date is batch1, that can be processed successfully. Batch2 is returned by an explicit call to its batch tag and can
 * be processed successfully as well. Its successor batch3 fails with a 404 Not Found.
 * <p>
 * Hence, after the execution of both runners, the federation_batch_info table should be the following:
 * <li>"expired_batch_tag" is deleted</li>
 * <li>"retry_batch_tag_successful" has state "PROCESSED"</li>
 * <li>"retry_batch_tag_fail" has state "ERROR_WONT_RETRY"</li>
 * <li>"batch1_tag" has state "PROCESSED"</li>
 * <li>"batch2_tag" has state "PROCESSED"</li>
 * <li>"batch3_tag" has state "ERROR"</li>
 * <li>no batch has state "UNPROCESSED"</li>
 * <p>
 * The diagnosis_key table should contain the data that correspond to the three batches with state "PROCESSED":
 * BATCH1_DATA, BATCH2_DATA and RETRY_BATCH_SUCCESSFUL_DATA
 */
@SpringBootTest
@ActiveProfiles("federation-download-integration")
@DirtiesContext
class DownloadIntegrationTest {

  public static final String BATCH1_DATA = "0123456789ABCDED";
  public static final String BATCH2_DATA = "0123456789ABCDEE";

  private static final String BATCH1_TAG = "batch1_tag";
  private static final String BATCH2_TAG = "batch2_tag";

  private static final String BATCH3_TAG = "batch3_tag";

  private static final String RETRY_BATCH_SUCCESSFUL_TAG = "retry_batch_tag_successful";
  private static final String RETRY_BATCH_SUCCESSFUL_DATA = "0123456789ABCDEF";

  private static final String RETRY_BATCH_FAILS_TAG = "retry_batch_tag_fail";
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
    DiagnosisKeyBatch batch2 = FederationBatchTestHelper.createDiagnosisKeyBatch(BATCH2_DATA);

    HttpHeaders batch3Headers = getHttpHeaders(BATCH3_TAG, EMPTY_BATCH_TAG);

    HttpHeaders retryBatchSuccessfulHeaders = getHttpHeaders(RETRY_BATCH_SUCCESSFUL_TAG, EMPTY_BATCH_TAG);
    DiagnosisKeyBatch retryBatchSuccessful = FederationBatchTestHelper.createDiagnosisKeyBatch(
        RETRY_BATCH_SUCCESSFUL_DATA);

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
                    .withHeaders(batch2Headers)
                    .withBody(batch2.toByteArray())));
    server.stubFor(
        get(anyUrl())
            .withHeader("batchTag", equalTo(BATCH3_TAG))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.NOT_FOUND.value())
                    .withHeaders(batch3Headers)));
    server.stubFor(
        get(anyUrl())
            .withHeader("batchTag", equalTo(RETRY_BATCH_FAILS_TAG))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.NOT_FOUND.value())
                    .withHeaders(batch3Headers)));
    server.stubFor(
        get(anyUrl())
            .withHeader("batchTag", equalTo(RETRY_BATCH_SUCCESSFUL_TAG))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeaders(retryBatchSuccessfulHeaders)
                    .withBody(retryBatchSuccessful.toByteArray())));
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
    assertThat(federationBatchInfoRepository.findAll()).hasSize(5);
    assertThat(federationBatchInfoRepository.findByStatus("UNPROCESSED")).isEmpty();
    assertThat(federationBatchInfoRepository.findByStatus("PROCESSED")).hasSize(3);
    assertThat(federationBatchInfoRepository.findByStatus("ERROR")).hasSize(1);
    assertThat(federationBatchInfoRepository.findByStatus("ERROR_WONT_RETRY")).hasSize(1);

    Iterable<DiagnosisKey> diagnosisKeys = diagnosisKeyRepository.findAll();
    assertThat(diagnosisKeys)
        .hasSize(3)
        .contains(FederationBatchTestHelper.createDiagnosisKey(BATCH1_DATA, downloadServiceConfig))
        .contains(FederationBatchTestHelper.createDiagnosisKey(BATCH2_DATA, downloadServiceConfig))
        .contains(FederationBatchTestHelper.createDiagnosisKey(RETRY_BATCH_SUCCESSFUL_DATA, downloadServiceConfig));
  }


}
