package app.coronawarn.server.services.download.runner;

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
import app.coronawarn.server.services.download.FederationBatchTestHelper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("federation-download-integration")
// TODO rename test
public class SpringContextRunnerTest {

  public static final String KEY_DATA_1 = "0123456789ABCDED";
  public static final String KEY_DATA_2 = "0123456789ABCDEE";
  public static final String KEY_DATA_3 = "0123456789ABCDEF";
  private static final String BATCH_TAG_1 = "19729de860ea";
  private static final String BATCH_TAG_2 = "19729de860eb";
  private static final String BATCH_TAG_3 = "19729de860ec";
  public static final String EMPTY_BATCH_TAG = "null";

  private static WireMockServer server;

  @Autowired
  private FederationBatchInfoRepository federationBatchInfoRepository;

  @Autowired
  private DiagnosisKeyRepository diagnosisKeyRepository;

  @BeforeAll
  static void setupWireMock() {
    HttpHeaders batchHeaders1 = getHttpHeaders(BATCH_TAG_1, BATCH_TAG_2);
    DiagnosisKeyBatch batch1 = FederationBatchTestHelper.createDiagnosisKeyBatch(KEY_DATA_1);

    HttpHeaders batchHeaders2 = getHttpHeaders(BATCH_TAG_2, BATCH_TAG_3);
    DiagnosisKeyBatch batch2 = FederationBatchTestHelper.createDiagnosisKeyBatch(KEY_DATA_2);

    HttpHeaders batchHeaders3 = getHttpHeaders(BATCH_TAG_3, EMPTY_BATCH_TAG);

    HttpHeaders retryBatchHeaders = getHttpHeaders("retry_batch_tag_1", EMPTY_BATCH_TAG);
    DiagnosisKeyBatch retryDiagnosisKeyBatch = FederationBatchTestHelper.createDiagnosisKeyBatch(KEY_DATA_3);

    server = new WireMockServer(options().port(1234));
    server.start();
    server.stubFor(
        get(anyUrl())
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeaders(batchHeaders1)
                    .withBody(batch1.toByteArray())));
    server.stubFor(
        get(anyUrl())
            .withHeader("batchTag", equalTo(BATCH_TAG_2))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeaders(batchHeaders2)
                    .withBody(batch2.toByteArray())));
    server.stubFor(
        get(anyUrl())
            .withHeader("batchTag", equalTo(BATCH_TAG_3))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.NOT_FOUND.value())
                    .withHeaders(batchHeaders3)));
    server.stubFor(
        get(anyUrl())
            .withHeader("batchTag", equalTo("retry_batch_tag_1"))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.NOT_FOUND.value())
                    .withHeaders(batchHeaders3)));
    server.stubFor(
        get(anyUrl())
            .withHeader("batchTag", equalTo("retry_batch_tag_2"))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeaders(retryBatchHeaders)
                    .withBody(retryDiagnosisKeyBatch.toByteArray())));
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
  //TODO failure messages in asserts
  void testDownloadRunSuccessfully() {
    assertThat(federationBatchInfoRepository.findAll()).hasSize(5);
    assertThat(federationBatchInfoRepository.findByStatus("UNPROCESSED")).hasSize(0);
    assertThat(federationBatchInfoRepository.findByStatus("PROCESSED")).hasSize(3);
    assertThat(federationBatchInfoRepository.findByStatus("ERROR")).hasSize(1);
    assertThat(federationBatchInfoRepository.findByStatus("ERROR_WONT_RETRY")).hasSize(1);

    Iterable<DiagnosisKey> diagnosisKeys = diagnosisKeyRepository.findAll();
    assertThat(diagnosisKeys).hasSize(3);
    assertThat(diagnosisKeys).contains(createDiagnosisKey(KEY_DATA_1));
    assertThat(diagnosisKeys).contains(createDiagnosisKey(KEY_DATA_2));
    assertThat(diagnosisKeys).contains(createDiagnosisKey(KEY_DATA_3));
  }

  private DiagnosisKey createDiagnosisKey(String keyData1) {
    return DiagnosisKey.builder()
        .fromFederationDiagnosisKey(FederationBatchTestHelper.createDiagnosisKey(keyData1)).build();
  }
}
