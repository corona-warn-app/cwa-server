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
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.download.config.DownloadServiceConfig;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.google.protobuf.ByteString;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * This integration test is responsible for testing the runners for downloading today's batches again. The Spring
 * profile "federation-download-today-again-integration" enables the test data generation in
 * /db/testdata-download-today-again/V99__createTestDataForDownloadTodayAgainIntegrationTest.sql via the
 * application-federation-download-today-again-integration.yaml.
 * <p>
 * The sql script for the test data contains a batch info for an already downloaded successfully processed batch today.
 * This test should verify that the download is triggered for the whole day again to incorporate changes of the batches
 * on the EFGS that might have happened since the last download.
 * <p>
 * The WireMockServer will additionally return a series of two batches:
 * <li>Batch1 is the first batch of the corresponding date..</li>
 * <li>Batch2 is the second batch of the corresponding date which is new on the EFGS.</li>
 * <p>
 * Hence, after the execution of both runners, the federation_batch_info table should be the following:
 * <li>"batch1_tag" has state "PROCESSED"</li>
 * <li>"batch2_tag" has state "PROCESSED"</li>
 * <p>
 * The diagnosis_key table should contain the data that correspond to the two batches with state "PROCESSED".
 */
@SpringBootTest
@ActiveProfiles("federation-enforce-download-again-integration")
@DirtiesContext
class DownloadEnforceDownloadAgainIntegrationTest {

  private static final String BATCH1_TAG = "batch1_tag";
  private static final String BATCH1_KEY_DATA = "0123456789ABCDEA";

  private static final String BATCH2_TAG = "batch2_tag";
  private static final String BATCH2_KEY_DATA = "0123456789ABCDEB";

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
    DiagnosisKeyBatch batch1 = FederationBatchTestHelper.createDiagnosisKeyBatch(BATCH1_KEY_DATA);

    HttpHeaders batch2Headers = getHttpHeaders(BATCH2_TAG, EMPTY_BATCH_TAG);
    DiagnosisKeyBatch batch2 = FederationBatchTestHelper.createDiagnosisKeyBatch(BATCH2_KEY_DATA);

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
    assertThat(federationBatchInfoRepository.findAll()).hasSize(2);
    assertThat(federationBatchInfoRepository.findByStatus("UNPROCESSED")).isEmpty();
    assertThat(federationBatchInfoRepository.findByStatus("PROCESSED")).hasSize(2);

    Iterable<DiagnosisKey> diagnosisKeys = diagnosisKeyRepository.findAll();
    assertThat(diagnosisKeys)
        .hasSize(2)
        .contains(FederationBatchTestHelper.createDiagnosisKey(BATCH1_KEY_DATA, downloadServiceConfig))
        .contains(FederationBatchTestHelper.createDiagnosisKey(BATCH2_KEY_DATA, downloadServiceConfig));
  }
}
