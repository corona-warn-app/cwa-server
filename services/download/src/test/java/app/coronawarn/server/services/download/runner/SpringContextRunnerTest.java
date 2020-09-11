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
import javax.annotation.PostConstruct;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
public class SpringContextRunnerTest {

  public static final String keyData1 = "0123456789ABCDED";
  public static final String keyData2 = "0123456789ABCDEE";
  public static final String keyData3 = "0123456789ABCDEF";
  private static final String batchTag1 = "19729de860ea";
  private static final String batchTag2 = "19729de860eb";
  private static final String batchTag3 = "19729de860ec";

  private static WireMockServer server;

  @Autowired
  private FederationBatchInfoRepository federationBatchInfoRepository;

  @Autowired
  private DiagnosisKeyRepository diagnosisKeyRepository;

  @BeforeAll
  static void setupWireMock() {
    HttpHeaders batchHeaders1 = getHttpHeaders(batchTag1, batchTag2);
    DiagnosisKeyBatch batch1 = FederationBatchTestHelper.createDiagnosisKeyBatch(keyData1);

    HttpHeaders batchHeaders2 = getHttpHeaders(batchTag2, batchTag3);
    DiagnosisKeyBatch batch2 = FederationBatchTestHelper.createDiagnosisKeyBatch(keyData2);

    HttpHeaders batchHeaders3 = getHttpHeaders(batchTag3, "null");

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
            .withHeader("batchTag", equalTo(batchTag2))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeaders(batchHeaders2)
                    .withBody(batch2.toByteArray())));
    server.stubFor(
        get(anyUrl())
            .withHeader("batchTag", equalTo(batchTag3))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.NOT_FOUND.value())
                    .withHeaders(batchHeaders3)));
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
  @DirtiesContext
  void testDownloadRunSuccessfully() {
    assertThat(federationBatchInfoRepository.findAll()).hasSize(3);
    assertThat(federationBatchInfoRepository.findByStatus("PROCESSED")).hasSize(2);
    assertThat(federationBatchInfoRepository.findByStatus("ERROR")).hasSize(1);

    Iterable<DiagnosisKey> diagnosisKeys = diagnosisKeyRepository.findAll();
    assertThat(diagnosisKeys).hasSize(2);
    assertThat(diagnosisKeys).contains(
        DiagnosisKey.builder()
            .fromFederationDiagnosisKey(FederationBatchTestHelper.createDiagnosisKey(keyData1)).build());
    assertThat(diagnosisKeys).contains(
        DiagnosisKey.builder()
            .fromFederationDiagnosisKey(FederationBatchTestHelper.createDiagnosisKey(keyData2)).build());
  }
}
