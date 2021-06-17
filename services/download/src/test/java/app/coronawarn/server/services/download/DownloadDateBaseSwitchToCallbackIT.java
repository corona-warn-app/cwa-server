package app.coronawarn.server.services.download;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
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
import java.util.function.Predicate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * This integration test is responsible for testing the switch between date base download strategy to
 * callback download strategy.
 * It ensure that there are no keys or batches conflicts after the switch and that all batches inserted
 * before the switch will still be processed. It also ensures that already processed batches won't cause a conflict
 * if an attempt to be re-inserted is made after the switch.
 */
@SpringBootTest
@ActiveProfiles({"connect-efgs","download-strategy-switch"})
@DirtiesContext
class DownloadDateBaseSwitchToCallbackIT {


  private static final String ERROR_BATCH_TAG = "error_batch";
  private static final String SWITCH_DAY_UNPROCESSED_BATCH_TAG = "switch_day_unprocessed_batch";
  private static final String SWITCH_DAY_NEW_UNPROCESSED_BATCH_TAG = "switch_day_new_unprocessed_batch";
  private static final String SWITCH_DAY_NEW_UNPROCESSED_1_BATCH_TAG = "switch_day_new_unprocessed_batch_1";

  private static final String SWITCH_DAY_ERROR_BATCH_TAG = "switch_day_error";

  private static final String VALID_BATCH_TAG = "valid_batch";
  private static final String VALID_BATCH_KEY1_DATA = "0123456789ABCDEA";
  private static final String VALID_BATCH_KEY2_DATA = "0123456789ABCDEB";
  private static final String VALID_BATCH_KEY3_DATA = "0123456789ABCDEC";
  private static final String VALID_BATCH_KEY4_DATA = "0123456789ABCDED";

  private static final String BATCH_WITH_NEXT_BATCH_TAG = "batch_with_next_batch";
  private static final String BATCH_WITH_NEXT_BATCH_DATA = "0123456789ABCDEF";
  private static final String ARBITRARY_NEXT_BATCH_TAG_1 = "next_pointer_1";
  private static final String ARBITRARY_NEXT_BATCH_TAG_2 = "next_pointer_2";

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
    HttpHeaders validBatchHeaders = getHttpHeaders(VALID_BATCH_TAG, EMPTY_BATCH_TAG);
    DiagnosisKeyBatch validBatch = FederationBatchTestHelper.createDiagnosisKeyBatch(
        List.of(
            FederationBatchTestHelper.createFederationDiagnosisKeyWithKeyData(VALID_BATCH_KEY1_DATA),
            FederationBatchTestHelper
                .createBuilderForValidFederationDiagnosisKey()
                .setKeyData(ByteString.copyFromUtf8(VALID_BATCH_KEY2_DATA))
                .setReportType(ReportType.CONFIRMED_CLINICAL_DIAGNOSIS)
                .build(),
            FederationBatchTestHelper
                .createBuilderForValidFederationDiagnosisKey()
                .setKeyData(ByteString.copyFromUtf8(VALID_BATCH_KEY3_DATA))
                .build(),
            FederationBatchTestHelper.createBuilderForValidFederationDiagnosisKey()
                .setKeyData(ByteString.copyFromUtf8(VALID_BATCH_KEY4_DATA))
                .setTransmissionRiskLevel(1)
                .setReportType(ReportType.CONFIRMED_CLINICAL_DIAGNOSIS)
                .build()
        )
    );

    HttpHeaders batchWithNextBatchTagHeaders = getHttpHeaders(BATCH_WITH_NEXT_BATCH_TAG, ARBITRARY_NEXT_BATCH_TAG_1);
    DiagnosisKeyBatch batchWithNextBatchTag = FederationBatchTestHelper
        .createDiagnosisKeyBatch(BATCH_WITH_NEXT_BATCH_DATA);

    HttpHeaders batchWithNextBatchTagHeaders2 = getHttpHeaders(ARBITRARY_NEXT_BATCH_TAG_1, ARBITRARY_NEXT_BATCH_TAG_2);
    DiagnosisKeyBatch batchWithNextBatchTag2 = FederationBatchTestHelper
        .createDiagnosisKeyBatch(BATCH_WITH_NEXT_BATCH_DATA);

    server = new WireMockServer(options().port(1234));
    server.start();

    server.stubFor(
        get(anyUrl())
            .withHeader("batchTag", containing(ERROR_BATCH_TAG))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeaders(validBatchHeaders)
                    .withBody(validBatch.toByteArray())));

    server.stubFor(
        get(anyUrl())
            .withHeader("batchTag", containing(SWITCH_DAY_UNPROCESSED_BATCH_TAG))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeaders(validBatchHeaders)
                    .withBody(validBatch.toByteArray())));

    server.stubFor(
        get(anyUrl())
            .withHeader("batchTag", equalTo(BATCH_WITH_NEXT_BATCH_TAG))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeaders(batchWithNextBatchTagHeaders)
                    .withBody(batchWithNextBatchTag.toByteArray())));

    server.stubFor(
        get(anyUrl())
            .withHeader("batchTag", equalTo(ARBITRARY_NEXT_BATCH_TAG_1))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeaders(batchWithNextBatchTagHeaders2)
                    .withBody(batchWithNextBatchTag2.toByteArray())));

    server.stubFor(
        get(anyUrl())
            .withHeader("batchTag", equalTo(ARBITRARY_NEXT_BATCH_TAG_2))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeaders(validBatchHeaders)
                    .withBody(validBatch.toByteArray())));

    server.stubFor(
        get(anyUrl())
            .withHeader("batchTag", containing(SWITCH_DAY_NEW_UNPROCESSED_BATCH_TAG))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeaders(validBatchHeaders)
                    .withBody(validBatch.toByteArray())));

    server.stubFor(
        get(anyUrl())
            .withHeader("batchTag", containing(SWITCH_DAY_ERROR_BATCH_TAG))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.NOT_FOUND.value())));
  }

  private static HttpHeaders getHttpHeaders(String batchTag, String nextBatchTag) {
    return new HttpHeaders()
        .plus(new HttpHeader(CONTENT_TYPE, "application/protobuf; version=1.0"))
        .plus(new HttpHeader("batchTag", batchTag))
        .plus(new HttpHeader("nextBatchTag", nextBatchTag));
  }

  private static Predicate<FederationBatchInfo> containsBatchTag(String batchName) {
    return federationBatchInfo -> federationBatchInfo.getBatchTag().contains(batchName);
  }

  @AfterAll
  static void tearDown() {
    server.stop();
  }

  @Test
  void testCorrectBatchesAfterSwitch() {
    assertThat(federationBatchInfoRepository.findAll()).hasSize(21);

    List<FederationBatchInfo> proccessed = federationBatchInfoRepository.findByStatus("PROCESSED");
    assertThat(proccessed).hasSize(18);

    assertThat(proccessed.stream().filter(containsBatchTag("switch_day_unprocessed_batch_1"))).hasSize(1);
    assertThat(proccessed.stream().filter(containsBatchTag("switch_day_unprocessed_batch_2"))).hasSize(1);
    assertThat(proccessed.stream().filter(containsBatchTag("switch_day_unprocessed_batch_3"))).hasSize(1);

    assertThat(federationBatchInfoRepository.findByStatus("UNPROCESSED")).isEmpty();
    assertThat(federationBatchInfoRepository.findByStatus("ERROR_WONT_RETRY")).hasSize(3);

    Iterable<DiagnosisKey> diagnosisKeys = diagnosisKeyRepository.findAll();
    assertThat(diagnosisKeys)
        .hasSize(5)
        .contains(FederationBatchTestHelper.createDiagnosisKey(VALID_BATCH_KEY1_DATA, downloadServiceConfig))
        .contains(FederationBatchTestHelper.createDiagnosisKey(VALID_BATCH_KEY2_DATA, downloadServiceConfig))
        .contains(FederationBatchTestHelper.createDiagnosisKey(VALID_BATCH_KEY3_DATA, downloadServiceConfig))
        .contains(FederationBatchTestHelper.createDiagnosisKey(VALID_BATCH_KEY4_DATA, downloadServiceConfig));
  }

  @Test
  void testNextBatchIdHandling() {
    assertThat(federationBatchInfoRepository.findAll()).hasSize(21);

    List<FederationBatchInfo> proccessed = federationBatchInfoRepository.findByStatus("PROCESSED");
    assertThat(proccessed).hasSize(18);

    // next batch is not handled for callback strategy.
    assertThat(proccessed.stream().filter(containsBatchTag(ARBITRARY_NEXT_BATCH_TAG_1))).hasSize(1);
    assertThat(proccessed.stream().filter(containsBatchTag(ARBITRARY_NEXT_BATCH_TAG_2))).hasSize(1);
  }

}
