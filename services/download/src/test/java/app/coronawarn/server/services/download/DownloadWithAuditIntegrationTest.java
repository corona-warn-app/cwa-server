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
import java.time.LocalDate;
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
 * This integration test is responsible for testing the runners for download while using the auditing download logic.
 * The Spring profile "batch-audit-test" enables the auditing of the downloaded batch tags.
 * <p>
 * The WireMockServer will additionally return a series of two batches:
 * </p>
 * <li>Batch1 is the first batch of the corresponding date. All containing diagnosis keys will be processed
 * successfully.
 * <li>Batch2 is returned by an explicit call to its batch tag and cannot be processed successfully.</li>
 * <p>
 * Hence, after the execution of both runners, the federation_batch_info table should be the following:
 * <li>"batch1_tag" has state "PROCESSED"</li>
 * <li>"batch2_tag" has state "ERROR"</li>
 * <li>no batch has state "UNPROCESSED"</li>
 * <p>
 * The diagnosis_key table should contain the data that correspond to the one batches with state "PROCESSED":
 * BATCH1_DATA.
 */
@SpringBootTest
@ActiveProfiles({"batch-audit-test", "disable-ssl-efgs-verification"})
@DirtiesContext
class DownloadWithAuditIntegrationTest {

  private static final String BATCH1_TAG = "batch1_tag";
  private static final String BATCH1_KEY1_DATA = "0123456789ABCDEA";
  private static final String BATCH1_KEY2_DATA = "0123456789ABCDEB";
  private static final String BATCH1_KEY3_DATA = "0123456789ABCDEC";
  private static final String BATCH1_KEY4_DATA = "0123456789ABCDED";

  private static final String BATCH2_TAG = "batch2_tag";
  private static final String BATCH2_KEY_DATA = "0123456789ABCDEE";

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
                .setKeyData(ByteString.copyFromUtf8(BATCH1_KEY1_DATA))
                .setTransmissionRiskLevel(Integer.MAX_VALUE)
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
                .setReportType(ReportType.CONFIRMED_CLINICAL_DIAGNOSIS)
                .build(),
            FederationBatchTestHelper.createBuilderForValidFederationDiagnosisKey()
                .setKeyData(ByteString.copyFromUtf8(BATCH1_KEY4_DATA))
                .setReportType(ReportType.CONFIRMED_CLINICAL_DIAGNOSIS)
                .build()
        )
    );

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

    server.stubFor(get("/diagnosiskeys/audit/download/" + LocalDate.now().toString() + "/" + BATCH1_TAG)
        .willReturn(
            aResponse()
                .withStatus(HttpStatus.OK.value())
        ));

    server.stubFor(get("/diagnosiskeys/audit/download/" + LocalDate.now().toString() + "/" + BATCH2_TAG)
        .willReturn(
            aResponse()
                .withStatus(HttpStatus.NOT_FOUND.value())
        ));
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
    assertThat(federationBatchInfoRepository.findByStatus("PROCESSED")).hasSize(1);
    assertThat(federationBatchInfoRepository.findByStatus("ERROR")).hasSize(1);

    Iterable<DiagnosisKey> diagnosisKeys = diagnosisKeyRepository.findAll();
    assertThat(diagnosisKeys)
        .hasSize(4)
        .contains(FederationBatchTestHelper.createDiagnosisKey(BATCH1_KEY1_DATA, downloadServiceConfig))
        .contains(FederationBatchTestHelper.createDiagnosisKey(BATCH1_KEY2_DATA, downloadServiceConfig))
        .contains(FederationBatchTestHelper.createDiagnosisKey(BATCH1_KEY3_DATA, downloadServiceConfig))
        .contains(FederationBatchTestHelper.createDiagnosisKey(BATCH1_KEY4_DATA, downloadServiceConfig));
  }
}
