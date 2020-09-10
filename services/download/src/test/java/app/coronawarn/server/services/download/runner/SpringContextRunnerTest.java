package app.coronawarn.server.services.download.runner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.federation.client.download.BatchDownloadResponse;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.download.FederationBatchUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.Optional;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.google.protobuf.ByteString;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
public class SpringContextRunnerTest {

  private static final String EXP_BATCH_TAG = "507f191e810c19729de860ea";
  private static final String EXP_NEXT_BATCH_TAG = "507f191e810c19729de860ea";
  private static final DiagnosisKeyBatch EXP_DIAGNOSIS_KEY_BATCH = DiagnosisKeyBatch.newBuilder()
      .addKeys(
          DiagnosisKey.newBuilder()
              .setKeyData(ByteString.copyFromUtf8("0123456789ABCDEF"))
              .addVisitedCountries("DE")
              .setRollingStartIntervalNumber(0)
              .setRollingPeriod(144)
              .setTransmissionRiskLevel(2)
              .build()).build();

  @SpyBean
  private FederationGatewayClient federationGatewayClient;

  @MockBean
  private FederationBatchInfoService federationBatchInfoService;

  private static WireMockServer server;

  @BeforeAll
  static void setupWireMock() {
    server = new WireMockServer(options().port(1234));
    HttpHeaders headers = new HttpHeaders();
    headers.plus(new HttpHeader(CONTENT_TYPE, "application/protobuf; version=1.0"));
    headers.plus(new HttpHeader("batchTag", EXP_BATCH_TAG));
    headers.plus(new HttpHeader("nextBatchTag", EXP_NEXT_BATCH_TAG));

    server.start();
    server.stubFor(
        get(anyUrl())
          .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withHeaders(headers).withBody(EXP_DIAGNOSIS_KEY_BATCH.toByteArray()))
    );
  }

  @AfterAll
  static void tearDown() {
    server.stop();
  }

  /*
  @TestConfiguration
  public static class TestConfig {

    @Primary
    @Bean
    public FederationGatewayClient mockFederationGatewayClient(FederationGatewayClient mockFederationGatewayClient) {
      // FederationGatewayClient federationGatewayClient = mock(FederationGatewayClient.class);
      // BatchDownloadResponse serverResponse = FederationBatchUtils.createBatchDownloadResponse("abc", Optional.empty());
      // when(federationGatewayClient.getDiagnosisKeys(anyString())).thenReturn(serverResponse);
      // return federationGatewayClient;
      return spy(mockFederationGatewayClient);
    }
  }

   */

  @Test
  @DirtiesContext
  void testDownloadRunSuccessfully() {
    verify(federationGatewayClient, times(1)).getDiagnosisKeys(anyString());
    verify(federationGatewayClient, never()).getDiagnosisKeys(anyString(), anyString());
    verify(federationBatchInfoService, times(1)).applyRetentionPolicy(anyInt());
  }
}
