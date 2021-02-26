package app.coronawarn.server.services.download;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import app.coronawarn.server.common.persistence.domain.FederationBatchTarget;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext
public abstract class GatewayServiceIntegrationSuite {


  public static final String BATCH1_DATA = "0123456789ABCDEF";
  private static final String BATCH1_TAG = "batch1_tag";
  private static final String BATCH2_TAG = "batch2_tag";


  private static WireMockServer wiremock = new WireMockServer(options().port(1234));

  @BeforeAll
  static void setupStubs() {
    DiagnosisKeyBatch batch1 = FederationBatchTestHelper.createDiagnosisKeyBatch(BATCH1_DATA);
    HttpHeaders batch1Headers = getHttpHeaders(BATCH1_TAG, BATCH2_TAG);
    wiremock.start();
    wiremock.stubFor(get("/diagnosiskeys/download/" + LocalDate.now().toString())
        .willReturn(aResponse()
            .withStatus(HttpStatus.OK.value())
            .withHeaders(batch1Headers)
            .withBody(batch1.toByteArray())));
    wiremock.stubFor(
        get("/diagnosiskeys/download/" + LocalDate.now().toString())
            .withHeader("batchTag", equalTo(BATCH2_TAG))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.NOT_FOUND.value())));

  }

  @AfterAll
  static void tearDown() {
    wiremock.stop();
  }

  private static HttpHeaders getHttpHeaders(String batchTag, String nextBatchTag) {
    return new HttpHeaders()
        .plus(new HttpHeader(CONTENT_TYPE, "application/protobuf; version=1.0"))
        .plus(new HttpHeader("batchTag", batchTag))
        .plus(new HttpHeader("nextBatchTag", nextBatchTag));
  }

  abstract void downloadShouldRunSuccessfulFor(FederationBatchTarget target);
}
