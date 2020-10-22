package app.coronawarn.server.services.download;


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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@SpringBootTest
@ActiveProfiles("federation-download-via-callback-integration")
@DirtiesContext
public class DownloadViaCallbackIntegrationTest {

  private static final String BATCH1_TAG = "batch1_tag";
  private static final String BATCH1_KEY1_DATA = "0123456789ABCDEA";
  private static final String BATCH1_KEY2_DATA = "0123456789ABCDEB";
  private static final String BATCH1_KEY3_DATA = "0123456789ABCDEC";
  private static final String BATCH1_KEY4_DATA = "0123456789ABCDED";

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
    HttpHeaders batch1Headers = getHttpHeaders(BATCH1_TAG);
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
                .setReportType(ReportType.SELF_REPORT)
                .build(),
            FederationBatchTestHelper
                .createBuilderForValidFederationDiagnosisKey()
                .setKeyData(ByteString.copyFromUtf8(BATCH1_KEY3_DATA))
                .setRollingPeriod(-5)
                .build()
        )
    );

    HttpHeaders retryBatchSuccessfulHeaders = getHttpHeaders(RETRY_BATCH_SUCCESSFUL_TAG);
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
  }

  private static HttpHeaders getHttpHeaders(String batchTag) {
    return new HttpHeaders()
        .plus(new HttpHeader(CONTENT_TYPE, "application/protobuf; version=1.0"))
        .plus(new HttpHeader("batchTag", batchTag))
        .plus(new HttpHeader("nextBatchTag", DownloadViaCallbackIntegrationTest.EMPTY_BATCH_TAG));
  }

  @AfterAll
  static void tearDown() {
    server.stop();
  }

  @Test
  void testDownloadRunSuccessfully() {
    assertThat(federationBatchInfoRepository.findAll()).hasSize(4);
    assertThat(federationBatchInfoRepository.findByStatus("UNPROCESSED")).isEmpty();
    assertThat(federationBatchInfoRepository.findByStatus("PROCESSED")).hasSize(2);
    //assertThat(federationBatchInfoRepository.findByStatus("PROCESSED_WITH_ERROR")).hasSize(1);
    assertThat(federationBatchInfoRepository.findByStatus("ERROR")).hasSize(1);
    assertThat(federationBatchInfoRepository.findByStatus("ERROR_WONT_RETRY")).hasSize(1);

    Iterable<DiagnosisKey> diagnosisKeys = diagnosisKeyRepository.findAll();
    assertThat(diagnosisKeys)
        .hasSize(3)
        .contains(FederationBatchTestHelper.createDiagnosisKey(BATCH1_KEY1_DATA, downloadServiceConfig))
        .contains(FederationBatchTestHelper.createDiagnosisKey(BATCH1_KEY4_DATA, downloadServiceConfig))
        .contains(FederationBatchTestHelper.createDiagnosisKey(RETRY_BATCH_SUCCESSFUL_KEY_DATA, downloadServiceConfig));
  }

}
