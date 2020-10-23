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
 * This integration test is responsible for testing the runners for download and retention policy and it is using the
 * download-via-callback logic. The Spring profile "federation-download-integration" enables the test data generation in
 * /db/testdata/V99__createTestDataForDownloadDateBasedIntegrationTest.sql via the
 * application-download-date-based-integration-test.yaml.
 * <p>
 * The sql script for the test data contains
 * <li>a batch info (expired_batch_tag) for an expired batch that should be deleted by the retention policy</li>
 * <li>a batch info (valid_tag) for a fully valid batch that should be processed</li>
 * <li>a batch info (processing_fails_partially) for a valid batch that should be processed with errors</li>
 * <li>a batch info (processing_fails) for an invalid batch for which the processing should fail with an error</li>
 * <li>a batch info (batch_with_next_batch_tag) for a valid batch that has a nextBatchTag that should not be processed
 * afterwards</li>
 * <li>and two batch info (retry_batch_tag_successful, retry_batch_tag_fails) from the current date of status 'ERROR',
 * which should be reprocessed.</li>
 * One of them will be successfully reprocessed and the other one will fail. The WireMockServer is configured
 * accordingly.
 * <p>
 * The WireMockServer will return a series of five batches, which correspond to the batches specified in the sql
 * script:
 * <li>Batch1 is the fully valid batch. All diagnosis key can be processed
 * successfully.</li>
 * <li>Batch2 is a valid batch that contains one key with invalid DSOS. It can be processed with errors.</li>
 * <li>Batch3 fails with a 404 Not Found.</li>
 *
 * <li>RETRY_BATCH_SUCCESSFUL can be processed successfully.</li>
 * <li>RETRY_BATCH_FAILS cannot be proccessed.</li>
 * <p>
 * Hence, after the execution of both runners, the federation_batch_info table should be the following:
 * <li>"expired_batch_tag" is deleted</li>
 * <li>"retry_batch_tag_successful" has state "PROCESSED"</li>
 * <li>"retry_batch_tag_fail" has state "ERROR_WONT_RETRY"</li>
 * <li>"batch_with_next_batch_tag" has state "PROCESSED", the batch referenced via nextBatchTag is not stored</li>
 * <li>"valid_tag" has state "PROCESSED"</li>
 * <li>"processing_fails_partially" has state "PROCESSED_WITH_ERROR"</li>
 * <li>"processing_fails" has state "ERROR"</li>
 * <li>no batch has state "UNPROCESSED"</li>
 * <p>
 */
@SpringBootTest
@ActiveProfiles("download-via-callback-integration-test")
@DirtiesContext
public class DownloadViaCallbackIntegrationTest {

  public static final String BATCH1_TAG = "valid_tag";
  private static final String BATCH1_KEY1_DATA = "0123456789ABCDEA";
  private static final String BATCH1_KEY2_DATA = "0123456789ABCDEB";
  private static final String BATCH1_KEY3_DATA = "0123456789ABCDEC";
  private static final String BATCH1_KEY4_DATA = "0123456789ABCDED";

  public static final String BATCH2_TAG = "processing_fails_partially";
  public static final String BATCH3_TAG = "processing_fails";
  private static final String RETRY_BATCH_SUCCESSFUL_TAG = "retry_batch_tag_successful";
  private static final String RETRY_BATCH_SUCCESSFUL_KEY_DATA = "0123456789ABCDEF";

  private static final String RETRY_BATCH_FAILS_TAG = "retry_batch_tag_fail";
  private static final String EMPTY_BATCH_TAG = "null";

  private static final String BATCH_WITH_NEXT_BATCH_TAG = "batch_with_next_batch_tag";
  private static final String BATCH_WITH_NEXT_BATCH_DATA = "0123456789ABCDF0";
  private static final String ARBITRARY_NEXT_BATCH_TAG = "next";

  private static WireMockServer server;

  @Autowired
  private FederationBatchInfoRepository federationBatchInfoRepository;

  @Autowired
  private DiagnosisKeyRepository diagnosisKeyRepository;

  @Autowired
  private DownloadServiceConfig downloadServiceConfig;

  @BeforeAll
  static void setupWireMock() {
    HttpHeaders batch1Headers = getHttpHeaders(BATCH1_TAG, EMPTY_BATCH_TAG);
    DiagnosisKeyBatch batch1 = FederationBatchTestHelper.createDiagnosisKeyBatch(
        List.of(
            FederationBatchTestHelper.createFederationDiagnosisKeyWithKeyData(BATCH1_KEY1_DATA),
            FederationBatchTestHelper.createBuilderForValidFederationDiagnosisKey()
                .setKeyData(ByteString.copyFromUtf8(BATCH1_KEY4_DATA))
                .setTransmissionRiskLevel(1)
                .setReportType(ReportType.CONFIRMED_CLINICAL_DIAGNOSIS)
                .build(),
            FederationBatchTestHelper
                .createBuilderForValidFederationDiagnosisKey()
                .setKeyData(ByteString.copyFromUtf8(BATCH1_KEY2_DATA))
                .setReportType(ReportType.CONFIRMED_CLINICAL_DIAGNOSIS)
                .build(),
            FederationBatchTestHelper
                .createBuilderForValidFederationDiagnosisKey()
                .setKeyData(ByteString.copyFromUtf8(BATCH1_KEY3_DATA))
                .build()
        )
    );

    HttpHeaders batch2Headers = getHttpHeaders(BATCH2_TAG, EMPTY_BATCH_TAG);
    DiagnosisKeyBatch batch2 = FederationBatchTestHelper.createDiagnosisKeyBatch(
        List.of(
            FederationBatchTestHelper.createFederationDiagnosisKeyWithDsos(9999)
        )
    );

    HttpHeaders batchWithNextBatchTagHeaders = getHttpHeaders(BATCH_WITH_NEXT_BATCH_TAG, ARBITRARY_NEXT_BATCH_TAG);
    DiagnosisKeyBatch batchWithNextBatchTag = FederationBatchTestHelper
        .createDiagnosisKeyBatch(BATCH_WITH_NEXT_BATCH_DATA);

    HttpHeaders retryBatchSuccessfulHeaders = getHttpHeaders(RETRY_BATCH_SUCCESSFUL_TAG, EMPTY_BATCH_TAG);
    DiagnosisKeyBatch retryBatchSuccessful = FederationBatchTestHelper.createDiagnosisKeyBatch(
        RETRY_BATCH_SUCCESSFUL_KEY_DATA);

    server = new WireMockServer(options().port(1234));
    server.start();

    server.stubFor(
        get(anyUrl())
            .withHeader("batchTag", equalTo(BATCH1_TAG))
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
                    .withStatus(HttpStatus.NOT_FOUND.value())));

    server.stubFor(
        get(anyUrl())
            .withHeader("batchTag", equalTo(RETRY_BATCH_FAILS_TAG))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.NOT_FOUND.value())));

    server.stubFor(
        get(anyUrl())
            .withHeader("batchTag", equalTo(RETRY_BATCH_SUCCESSFUL_TAG))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeaders(retryBatchSuccessfulHeaders)
                    .withBody(retryBatchSuccessful.toByteArray())));

    server.stubFor(
        get(anyUrl())
            .withHeader("batchTag", equalTo(BATCH_WITH_NEXT_BATCH_TAG))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeaders(batchWithNextBatchTagHeaders)
                    .withBody(batchWithNextBatchTag.toByteArray())));
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
    assertThat(federationBatchInfoRepository.findAll()).hasSize(6);
    assertThat(federationBatchInfoRepository.findByStatus("UNPROCESSED")).isEmpty();
    assertThat(federationBatchInfoRepository.findByStatus("PROCESSED")).hasSize(3);
    assertThat(federationBatchInfoRepository.findByStatus("PROCESSED_WITH_ERROR")).hasSize(1);
    assertThat(federationBatchInfoRepository.findByStatus("ERROR")).hasSize(1);
    assertThat(federationBatchInfoRepository.findByStatus("ERROR_WONT_RETRY")).hasSize(1);

    Iterable<DiagnosisKey> diagnosisKeys = diagnosisKeyRepository.findAll();
    assertThat(diagnosisKeys)
        .hasSize(6)
        .contains(FederationBatchTestHelper.createDiagnosisKey(BATCH1_KEY1_DATA, downloadServiceConfig))
        .contains(FederationBatchTestHelper.createDiagnosisKey(BATCH1_KEY2_DATA, downloadServiceConfig))
        .contains(FederationBatchTestHelper.createDiagnosisKey(BATCH1_KEY3_DATA, downloadServiceConfig))
        .contains(FederationBatchTestHelper.createDiagnosisKey(BATCH1_KEY4_DATA, downloadServiceConfig))
        .contains(FederationBatchTestHelper.createDiagnosisKey(RETRY_BATCH_SUCCESSFUL_KEY_DATA, downloadServiceConfig))
        .contains(FederationBatchTestHelper.createDiagnosisKey(BATCH_WITH_NEXT_BATCH_DATA, downloadServiceConfig));
  }

}
