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
 * This integration test is responsible for testing the runners for download and retention policy while using the
 * date-based download logic. The Spring profile "federation-download-integration" enables the test data generation in
 * /db/testdata/V99__createTestDataForDownloadDateBasedIntegrationTest.sql via the
 * application-download-date-based-integration-test.yaml.
 * <p>
 * The sql script for the test data contains
 * <li>a batch info for an expired batch that should be deleted by the retention policy</li>
 * <li>and two batch info from the previous day of status 'ERROR', which should be reprocessed.</li>
 * One of them will be successfully reprocessed and the other one will fail. The WireMockServer is configured
 * accordingly.
 * <p>
 * The WireMockServer will additionally return a series of three batches:
 * <li>Batch1 is the first batch of the corresponding date. The first diagnosis key can be processed
 * successfully. The second diagnosis key is rejected due to its unsupported ReportType "Self Reported". The third
 * diagnosis key is rejected due to its invalid RollingPeriod. The fourth diagnosis key can be processed successfully,
 * but its ReportType is CONFIRMED_CLINICAL_DIAGNOSIS which should be changed to CONFIRMED_TEST during the download. The
 * fifth diagnosis key without having supported countries should be defaulted to the origin country of the key.</li>
 * <li>Batch2 is returned by an explicit call to its batch tag and can be processed successfully as well.</li>
 * <li>Batch3 fails with a 404 Not Found.</li>
 * <p>
 * Hence, after the execution of both runners, the federation_batch_info table should be the following:
 * <li>"expired_batch_tag" is deleted</li>
 * <li>"retry_batch_tag_successful" has state "PROCESSED"</li>
 * <li>"retry_batch_tag_fail" has state "ERROR_WONT_RETRY"</li>
 * <li>"batch1_tag" has state "PROCESSED_WITH_ERROR"</li>
 * <li>"batch2_tag" has state "PROCESSED"</li>
 * <li>"batch3_tag" has state "ERROR"</li>
 * <li>no batch has state "UNPROCESSED"</li>
 * <p>
 * The diagnosis_key table should contain the data that correspond to the three batches with state "PROCESSED":
 * BATCH1_DATA, BATCH2_DATA and RETRY_BATCH_SUCCESSFUL_DATA
 */
@SpringBootTest
@ActiveProfiles("download-date-based-integration-test")
@DirtiesContext
class DownloadDateBasedIntegrationTest {

  private static final String BATCH1_TAG = "batch1_tag";
  private static final String BATCH1_KEY1_DATA = "0123456789ABCDEA";
  private static final String BATCH1_KEY2_DATA = "0123456789ABCDEB";
  private static final String BATCH1_KEY3_DATA = "0123456789ABCDEC";
  private static final String BATCH1_KEY4_DATA = "0123456789ABCDED";
  private static final String BATCH1_KEY5_DATA = "0123456789ABCDEG";

  private static final String BATCH2_TAG = "batch2_tag";
  private static final String BATCH2_KEY_DATA = "0123456789ABCDEE";

  private static final String BATCH3_TAG = "batch3_tag";

  private static final String RETRY_BATCH_SUCCESSFUL_TAG = "retry_batch_tag_successful";
  private static final String RETRY_BATCH_SUCCESSFUL_KEY_DATA = "0123456789ABCDEF";

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
    DiagnosisKeyBatch batch1 = FederationBatchTestHelper.createDiagnosisKeyBatch(
        List.of(
            FederationBatchTestHelper.createFederationDiagnosisKeyWithKeyData(BATCH1_KEY1_DATA),
            FederationBatchTestHelper.createBuilderForValidFederationDiagnosisKey()
                .setKeyData(ByteString.copyFromUtf8(BATCH1_KEY4_DATA))
                .setTransmissionRiskLevel(Integer.MAX_VALUE)
                .setReportType(ReportType.CONFIRMED_CLINICAL_DIAGNOSIS)
                .build(),
            FederationBatchTestHelper
                .createBuilderForValidFederationDiagnosisKey()
                .setKeyData(ByteString.copyFromUtf8(BATCH1_KEY2_DATA))
                .setReportType(ReportType.SELF_REPORT)
                .build(),
            FederationBatchTestHelper
                .createBuilderForValidFederationDiagnosisKey()
                .setKeyData(ByteString.copyFromUtf8(BATCH1_KEY3_DATA))
                .setRollingPeriod(-5)
                .build(),
            FederationBatchTestHelper.createBuilderForValidFederationDiagnosisKey()
                .setKeyData(ByteString.copyFromUtf8(BATCH1_KEY5_DATA))
                .clearVisitedCountries()
                .setOrigin("IT")
                .build()
        )
    );

    HttpHeaders batch2Headers = getHttpHeaders(BATCH2_TAG, BATCH3_TAG);
    DiagnosisKeyBatch batch2 = FederationBatchTestHelper.createDiagnosisKeyBatch(BATCH2_KEY_DATA);

    HttpHeaders batch3Headers = getHttpHeaders(BATCH3_TAG, EMPTY_BATCH_TAG);

    HttpHeaders retryBatchSuccessfulHeaders = getHttpHeaders(RETRY_BATCH_SUCCESSFUL_TAG, RETRY_BATCH_FAILS_TAG);
    DiagnosisKeyBatch retryBatchSuccessful = FederationBatchTestHelper.createDiagnosisKeyBatch(
        RETRY_BATCH_SUCCESSFUL_KEY_DATA);

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
                    .withHeaders(getHttpHeaders(RETRY_BATCH_FAILS_TAG, EMPTY_BATCH_TAG))));
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
    assertThat(federationBatchInfoRepository.findByStatus("PROCESSED")).hasSize(2);
    assertThat(federationBatchInfoRepository.findByStatus("PROCESSED_WITH_ERROR")).hasSize(1);
    assertThat(federationBatchInfoRepository.findByStatus("ERROR")).hasSize(1);
    assertThat(federationBatchInfoRepository.findByStatus("ERROR_WONT_RETRY")).hasSize(1);

    Iterable<DiagnosisKey> diagnosisKeys = diagnosisKeyRepository.findAll();
    assertThat(diagnosisKeys)
        .hasSize(5)
        .contains(FederationBatchTestHelper.createDiagnosisKey(BATCH1_KEY1_DATA, downloadServiceConfig))
        .contains(FederationBatchTestHelper.createDiagnosisKey(BATCH1_KEY4_DATA, downloadServiceConfig))
        .contains(FederationBatchTestHelper.createDiagnosisKey(BATCH2_KEY_DATA, downloadServiceConfig))
        .contains(FederationBatchTestHelper.createDiagnosisKey(RETRY_BATCH_SUCCESSFUL_KEY_DATA, downloadServiceConfig))
        .contains(FederationBatchTestHelper
            .createDiagnosisKeyForSpecificOriginCountry(BATCH1_KEY5_DATA, "IT", downloadServiceConfig));
  }
}
