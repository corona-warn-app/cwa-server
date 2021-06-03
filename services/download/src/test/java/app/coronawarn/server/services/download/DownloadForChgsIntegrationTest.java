package app.coronawarn.server.services.download;

import static app.coronawarn.server.common.persistence.domain.FederationBatchSourceSystem.EFGS;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchSourceSystem;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.repository.FederationBatchInfoRepository;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"connect-chgs", "enable-date-based-download", "error-batch"})

public class DownloadForChgsIntegrationTest extends GatewayServiceIntegrationSuite {

  @Autowired
  private FederationBatchInfoRepository batchInfoRepository;

  @BeforeAll
  static void setupStubs() {
    DiagnosisKeyBatch batch1 = FederationBatchTestHelper.createDiagnosisKeyBatch(BATCH1_DATA, "CH");
    HttpHeaders batch1Headers = getHttpHeaders(BATCH1_TAG, BATCH2_TAG);
    wiremock.start();
    wiremock.stubFor(get("/diagnosiskeys/download/" + LocalDate.now())
        .willReturn(aResponse()
            .withStatus(HttpStatus.OK.value())
            .withHeaders(batch1Headers)
            .withBody(batch1.toByteArray())));
    wiremock.stubFor(
        get("/diagnosiskeys/download/" + LocalDate.now())
            .withHeader("batchTag", equalTo(BATCH2_TAG))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.NOT_FOUND.value())));
    wiremock.stubFor(
        get("/diagnosiskeys/download/" + LocalDate.now())
            .withHeader("batchTag", equalTo("batchtag"))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.NOT_FOUND.value())));
  }

  @ParameterizedTest
  @EnumSource(value = FederationBatchSourceSystem.class, names = {"CHGS"})
  void downloadShouldRunSuccessfulFor(FederationBatchSourceSystem source) {
    final List<FederationBatchInfo> processedBatches = batchInfoRepository
        .findByStatusAndSourceSystem(FederationBatchStatus.PROCESSED.name(), FederationBatchSourceSystem.CHGS);
    final List<FederationBatchInfo> errorBatches = batchInfoRepository
        .findByStatusAndSourceSystem(FederationBatchStatus.ERROR.name(), FederationBatchSourceSystem.CHGS);
    final List<FederationBatchInfo> errorWontRetryBatches = batchInfoRepository
        .findByStatusAndSourceSystem(FederationBatchStatus.ERROR_WONT_RETRY.name(), FederationBatchSourceSystem.CHGS);
    assertThat(errorBatches).hasSize(1);
    assertThat(processedBatches).hasSize(1);
    assertThat(errorWontRetryBatches).hasSize(1); // This comes from the "ERROR" batch in the sql-file.
    assertThat(processedBatches).extracting(FederationBatchInfo::getSourceSystem).containsExactly(source);
  }

  @ParameterizedTest
  @EnumSource(value = FederationBatchSourceSystem.class, names = {"CHGS"})
  void downloadShouldDelete2Keys(FederationBatchSourceSystem source) {
    assertEquals(batchInfoRepository.countForDateAndSourceSystem(LocalDate.now(ZoneId.of("UTC")), source), 2);
    assertEquals(batchInfoRepository.countForDateAndSourceSystem(LocalDate.now(ZoneId.of("UTC")), EFGS), 2);
  }
}
