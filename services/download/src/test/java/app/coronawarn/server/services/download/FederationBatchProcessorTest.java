package app.coronawarn.server.services.download;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.domain.config.TekFieldDerivations;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.download.config.DownloadServiceConfig;
import app.coronawarn.server.services.download.validation.ValidFederationKeyFilter;
import com.google.protobuf.ByteString;
import feign.FeignException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static app.coronawarn.server.common.persistence.domain.FederationBatchSourceSystem.EFGS;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.*;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.util.Lists.list;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
@SpringBootTest(classes = {FederationBatchProcessor.class, FederationBatchInfoService.class, DiagnosisKeyService.class,
    FederationGatewayClient.class, ValidFederationKeyFilter.class, TekFieldDerivations.class})
@DirtiesContext
@EnableConfigurationProperties(value = DownloadServiceConfig.class)
@ActiveProfiles("connect-efgs")
class FederationBatchProcessorTest {

  private final LocalDate date = LocalDate.of(2020, 9, 1);
  private final String batchTag1 = "507f191e810c19729de860ea";
  private final String batchTag2 = "507f191e810c19729de860eb";

  @Autowired
  private DownloadServiceConfig config;

  @MockBean
  private FederationBatchInfoService batchInfoService;

  @MockBean
  private DiagnosisKeyService diagnosisKeyService;

  @MockBean
  private FederationGatewayDownloadService federationGatewayDownloadService;

  @Autowired
  private FederationBatchProcessor batchProcessor;

  @AfterEach
  void resetMocks() {
    reset(federationGatewayDownloadService);
    reset(diagnosisKeyService);
    reset(batchInfoService);
  }

  @BeforeEach
  void resetConfigToDefault() {
    config.setEnforceDateBasedDownload(false);
  }

  @Nested
  @DisplayName("prepareDownload")
  class PrepareDownload {

    @Test
    void testWithDateBasedDownload() throws Exception {
      FederationBatchProcessor batchProcessorSpy = Mockito.spy(batchProcessor);
      config.setEnforceDateBasedDownload(true);
      batchProcessorSpy.prepareDownload();

      LocalDate date = LocalDate.now(ZoneOffset.UTC).minus(Period.ofDays(config.getEnforceDownloadOffsetDays()));

      verify(batchInfoService, times(1)).deleteForDate(date, EFGS);
      verify(batchProcessorSpy, times(1)).saveFirstBatchInfoForDate(date);
    }

    @Test
    void testWithCallbackBasedDownload() throws Exception {
      FederationBatchProcessor batchProcessorSpy = Mockito.spy(batchProcessor);
      config.setEnforceDateBasedDownload(false);
      batchProcessorSpy.prepareDownload();

      verify(batchInfoService, never()).deleteForDate(any(), eq(EFGS));
      verify(batchProcessorSpy, never()).saveFirstBatchInfoForDate(any());
    }
  }

  @Nested
  @DisplayName("saveFirstBatchInfoForDateTest")
  class SaveFirstBatchInfoForDate {

    @Test
    void testBatchInfoForDateDoesNotExist() throws FatalFederationGatewayException, BatchDownloadException {
      BatchDownloadException batchDownloadException = new BatchDownloadException(null, LocalDate.now(), null);
      doThrow(batchDownloadException).when(federationGatewayDownloadService).downloadBatch(any());
      batchProcessor.saveFirstBatchInfoForDate(date);
      verify(batchInfoService, never()).save(any(FederationBatchInfo.class));
    }

    @Test
    void testBatchInfoForDateExists() throws Exception {
      BatchDownloadResponse serverResponse = FederationBatchTestHelper.createBatchDownloadResponse(batchTag1,
          Optional.empty());
      when(federationGatewayDownloadService.downloadBatch(date)).thenReturn(serverResponse);

      batchProcessor.saveFirstBatchInfoForDate(date);

      verify(batchInfoService, times(1)).save(new FederationBatchInfo(batchTag1, date, EFGS));
    }

    @Test
    void testBatchInfoForDateReturnsNull() throws Exception {
      when(federationGatewayDownloadService.downloadBatch(date)).thenReturn(null);

      batchProcessor.saveFirstBatchInfoForDate(date);

      verify(batchInfoService, never()).save(any());
    }

    @Test
    void testBatchInfoForTodayIsDeleted() throws Exception {
      LocalDate date = LocalDate.now(ZoneOffset.UTC);
      config.setEnforceDateBasedDownload(true);
      batchProcessor.prepareDownload();

      verify(batchInfoService, times(1)).deleteForDate(date, EFGS);
    }
  }

  @Nested
  @DisplayName("processUnprocessedFederationBatches")
  class ProcessUnprocessedFederationBatchesTest {

    @Test
    void testNoUnprocessedBatches() throws Exception {
      when(batchInfoService.findByStatus(any(FederationBatchStatus.class), any())).thenReturn(emptyList());
      batchProcessor.processUnprocessedFederationBatches();
      verify(federationGatewayDownloadService, never()).downloadBatch(anyString(), any());
      verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    }

    @Test
    void testOneUnprocessedBatchNoNextBatch() throws Exception {
      FederationBatchInfo federationBatchInfo = new FederationBatchInfo(batchTag1, date, UNPROCESSED, EFGS);
      when(batchInfoService.findByStatus(UNPROCESSED, EFGS)).thenReturn(list(federationBatchInfo));
      BatchDownloadResponse serverResponse = FederationBatchTestHelper.createBatchDownloadResponse(batchTag1,
          Optional.empty());
      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(serverResponse);

      batchProcessor.processUnprocessedFederationBatches();

      verify(batchInfoService, times(1)).updateStatus(federationBatchInfo, PROCESSED);
      verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(any());
    }

    @Test
    void testOneUnprocessedBatchOneNextBatch() throws Exception {
      config.setEnforceDateBasedDownload(true);
      FederationBatchInfo batchInfo1 = new FederationBatchInfo(batchTag1, date, UNPROCESSED, EFGS);
      FederationBatchInfo batchInfo2 = new FederationBatchInfo(batchTag2, date, UNPROCESSED, EFGS);
      when(batchInfoService.findByStatus(UNPROCESSED, EFGS)).thenReturn(list(batchInfo1));

      BatchDownloadResponse serverResponse1 = FederationBatchTestHelper.createBatchDownloadResponse(batchTag1,
          Optional.of(batchTag2));
      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(serverResponse1);
      BatchDownloadResponse serverResponse2 = FederationBatchTestHelper.createBatchDownloadResponse(batchTag2,
          Optional.empty());
      when(federationGatewayDownloadService.downloadBatch(batchTag2, date)).thenReturn(serverResponse2);

      batchProcessor.processUnprocessedFederationBatches();

      verify(batchInfoService, times(1)).findByStatus(UNPROCESSED, EFGS);

      verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      verify(batchInfoService, times(1)).updateStatus(batchInfo1, PROCESSED);

      verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag2, date);
      verify(batchInfoService, times(1)).updateStatus(batchInfo2, PROCESSED);

      verify(diagnosisKeyService, times(2)).saveDiagnosisKeys(any());
    }

    @Test
    void testOneUnprocessedBatchFails() throws Exception {
      when(batchInfoService.findByStatus(UNPROCESSED, EFGS))
          .thenReturn(list(new FederationBatchInfo(batchTag1, date, UNPROCESSED, EFGS)));
      doThrow(BatchDownloadException.class).when(federationGatewayDownloadService).downloadBatch(batchTag1, date);

      batchProcessor.processUnprocessedFederationBatches();

      verify(batchInfoService, times(1)).findByStatus(UNPROCESSED, EFGS);
      verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      verify(batchInfoService, times(1)).updateStatus(any(FederationBatchInfo.class), eq(ERROR));
      verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    }

    @Test
    void testOneUnprocessedBatchAuditFails() throws Exception {
      config.setBatchAuditEnabled(true);
      when(batchInfoService.findByStatus(UNPROCESSED, EFGS))
          .thenReturn(list(new FederationBatchInfo(batchTag1, date, UNPROCESSED, EFGS)));
      BatchDownloadResponse serverResponse = FederationBatchTestHelper.createBatchDownloadResponse(batchTag1,
          Optional.empty());
      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(serverResponse);
      doThrow(BatchAuditException.class).when(federationGatewayDownloadService).auditBatch(batchTag1, date);
      batchProcessor.processUnprocessedFederationBatches();
      verify(batchInfoService, times(1)).findByStatus(UNPROCESSED, EFGS);
      verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      verify(federationGatewayDownloadService, times(1)).auditBatch(batchTag1, date);
      verify(batchInfoService, times(1)).updateStatus(any(FederationBatchInfo.class), eq(ERROR));
      verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
      config.setBatchAuditEnabled(false);
    }

    @Test
    void testOneUnprocessedEmptyBatchNoAuditCall() throws Exception {
      config.setBatchAuditEnabled(true);
      when(batchInfoService.findByStatus(UNPROCESSED, EFGS))
          .thenReturn(list(new FederationBatchInfo(batchTag1, date, UNPROCESSED, EFGS)));
      BatchDownloadResponse serverResponse = FederationBatchTestHelper.createBatchDownloadResponse(batchTag1,
          Optional.empty());
      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(serverResponse);
      doThrow(BatchAuditException.class).when(federationGatewayDownloadService).auditBatch(batchTag1, date);
      batchProcessor.processUnprocessedFederationBatches();
      verify(batchInfoService, times(1)).findByStatus(UNPROCESSED, EFGS);
      verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      verify(federationGatewayDownloadService, times(1)).auditBatch(batchTag1, date);
      verify(batchInfoService, times(1)).updateStatus(any(FederationBatchInfo.class), eq(ERROR));
      verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
      config.setBatchAuditEnabled(false);
    }

    @Test
    void testNoInfiniteLoopSameBatchTag() throws FatalFederationGatewayException, BatchDownloadException {
      config.setEnforceDateBasedDownload(true);
      FederationBatchInfo batchInfo = new FederationBatchInfo(batchTag1, date, UNPROCESSED, EFGS);
      BatchDownloadResponse serverResponse = FederationBatchTestHelper.createBatchDownloadResponse(batchTag1,
          Optional.of(batchTag1));

      when(batchInfoService.findByStatus(UNPROCESSED, EFGS)).thenReturn(list(batchInfo));
      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(serverResponse);

      Assertions.assertTimeoutPreemptively(Duration.ofSeconds(1),
          () -> batchProcessor.processUnprocessedFederationBatches());
      verify(batchInfoService, times(1)).findByStatus(UNPROCESSED, EFGS);
      verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      verify(batchInfoService, times(1)).updateStatus(batchInfo, PROCESSED);
    }
  }

  @Nested
  @DisplayName("processErrorFederationBatches")
  class ProcessErrorFederationBatchesTest {

    @Test
    void testNoErrorBatches() throws Exception {
      when(batchInfoService.findByStatus(any(FederationBatchStatus.class), any())).thenReturn(emptyList());
      batchProcessor.processErrorFederationBatches();
      verify(batchInfoService, times(1)).findByStatus(ERROR, EFGS);
      verify(federationGatewayDownloadService, never()).downloadBatch(anyString(), any());
      verify(batchInfoService, never()).save(any(FederationBatchInfo.class));
    }

    @Test
    void testOneErrorBatchNoNextBatch() throws Exception {
      when(batchInfoService.findByStatus(ERROR, EFGS))
          .thenReturn(list(new FederationBatchInfo(batchTag1, date, ERROR, EFGS)));
      BatchDownloadResponse serverResponse = FederationBatchTestHelper.createBatchDownloadResponse(batchTag1,
          Optional.empty());
      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(serverResponse);

      batchProcessor.processErrorFederationBatches();

      verify(batchInfoService, times(1)).findByStatus(ERROR, EFGS);
      verify(federationGatewayDownloadService, times(1)).downloadBatch(anyString(), any());
      verify(batchInfoService, times(1)).updateStatus(any(FederationBatchInfo.class), eq(PROCESSED));
      verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(any());
    }

    @Test
    void testOneErrorBatchOneNextBatch() throws Exception {
      FederationBatchInfo batchInfo1 = new FederationBatchInfo(batchTag1, date, ERROR, EFGS);
      FederationBatchInfo batchInfo2 = new FederationBatchInfo(batchTag2, date, UNPROCESSED, EFGS);

      when(batchInfoService.findByStatus(ERROR, EFGS)).thenReturn(list(batchInfo1));

      BatchDownloadResponse serverResponse1 = FederationBatchTestHelper.createBatchDownloadResponse(batchTag1,
          Optional.of(batchTag2));
      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(serverResponse1);
      BatchDownloadResponse serverResponse2 = FederationBatchTestHelper.createBatchDownloadResponse(batchTag2,
          Optional.empty());
      when(federationGatewayDownloadService.downloadBatch(batchTag2, date)).thenReturn(serverResponse2);

      batchProcessor.processErrorFederationBatches();

      verify(batchInfoService, times(1)).findByStatus(ERROR, EFGS);
      verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      verify(batchInfoService, times(1)).updateStatus(batchInfo1, PROCESSED);

      verify(batchInfoService, times(1)).save(batchInfo2);
      verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(any());
    }

    @Test
    void testOneErrorBatchRetryNotFound() throws Exception {
      when(batchInfoService.findByStatus(ERROR, EFGS))
          .thenReturn(list(new FederationBatchInfo(batchTag1, date, ERROR, EFGS)));
      doThrow(FeignException.NotFound.class).when(federationGatewayDownloadService).downloadBatch(batchTag1, date);

      batchProcessor.processErrorFederationBatches();

      verify(batchInfoService, times(1)).findByStatus(ERROR, EFGS);
      verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      verify(batchInfoService, times(1)).updateStatus(any(FederationBatchInfo.class), eq(ERROR_WONT_RETRY));
      verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    }

    @Test
    void testOneErrorBatchSavingNextBatchInfoFails() throws Exception {
      when(batchInfoService.findByStatus(ERROR, EFGS))
          .thenReturn(list(new FederationBatchInfo(batchTag1, date, ERROR, EFGS)));
      doThrow(RuntimeException.class).when(batchInfoService).save(any(FederationBatchInfo.class));

      BatchDownloadResponse serverResponse = FederationBatchTestHelper.createBatchDownloadResponse(batchTag1,
          Optional.of(batchTag2));
      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(serverResponse);

      batchProcessor.processErrorFederationBatches();

      verify(batchInfoService, times(1)).findByStatus(ERROR, EFGS);
      verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      verify(batchInfoService, times(1)).updateStatus(any(FederationBatchInfo.class), eq(ERROR_WONT_RETRY));
      verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(any());
    }
  }

  @Nested
  @DisplayName("testKeyValidationInOneBatch")
  class TestKeyValidationInOneBatch {

    @Test
    void testFailureKeysAreSkipped() throws Exception {
      FederationBatchInfo batchInfo = new FederationBatchInfo(batchTag1, date, UNPROCESSED, EFGS);
      when(batchInfoService.findByStatus(UNPROCESSED, EFGS)).thenReturn(list(batchInfo));

      DiagnosisKey validKey = FederationBatchTestHelper.createBuilderForValidFederationDiagnosisKey().build();
      DiagnosisKey invalidKey = FederationBatchTestHelper
          .createFederationDiagnosisKeyWithKeyData(FederationBatchTestHelper.createByteStringOfLength(32));

      DiagnosisKeyBatch batch = FederationBatchTestHelper.createDiagnosisKeyBatch(List.of(validKey, invalidKey));
      BatchDownloadResponse downloadResponse = FederationBatchTestHelper.createBatchDownloadResponse(batchTag1,
          Optional.empty(), batch);

      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(downloadResponse);
      batchProcessor.processUnprocessedFederationBatches();

      verify(batchInfoService, times(1)).findByStatus(UNPROCESSED, EFGS);
      verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      verify(batchInfoService, times(1)).updateStatus(batchInfo, PROCESSED_WITH_ERROR);
      verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(any());
    }

    @Test
    void testDiagnosisKeyPassesDownloadValidationButBuildingFails() throws Exception {
      FederationBatchInfo batchInfo = new FederationBatchInfo(batchTag1, date, UNPROCESSED, EFGS);

      when(batchInfoService.findByStatus(UNPROCESSED, EFGS)).thenReturn(list(batchInfo));

      DiagnosisKey invalidKey = FederationBatchTestHelper.createBuilderForValidFederationDiagnosisKey()
          .setRollingPeriod(-5).build();
      DiagnosisKeyBatch batch = FederationBatchTestHelper.createDiagnosisKeyBatch(List.of(invalidKey));
      BatchDownloadResponse downloadResponse = FederationBatchTestHelper.createBatchDownloadResponse(batchTag1,
          Optional.empty(), batch);

      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(downloadResponse);
      batchProcessor.processUnprocessedFederationBatches();

      verifyProcessedWithStatus(batchInfo, PROCESSED_WITH_ERROR);
    }

    @ParameterizedTest
    @ValueSource(ints = {-15, -17, 4001})
    void testFailureInvalidDSOS(int invalidDsos) throws Exception {
      FederationBatchInfo batchInfo = new FederationBatchInfo(batchTag1, date, UNPROCESSED, EFGS);
      when(batchInfoService.findByStatus(UNPROCESSED, EFGS)).thenReturn(list(batchInfo));

      DiagnosisKey invalidKey = FederationBatchTestHelper.createBuilderForValidFederationDiagnosisKey()
          .setDaysSinceOnsetOfSymptoms(invalidDsos).build();
      DiagnosisKeyBatch batch = FederationBatchTestHelper.createDiagnosisKeyBatch(List.of(invalidKey));
      BatchDownloadResponse downloadResponse = FederationBatchTestHelper.createBatchDownloadResponse(batchTag1,
          Optional.empty(), batch);

      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(downloadResponse);
      batchProcessor.processUnprocessedFederationBatches();

      verifyProcessedWithStatus(batchInfo, PROCESSED_WITH_ERROR);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 15, 17, 20})
    void testFailureInvalidKeyDataLength(int invalidKeyDataLength) throws Exception {
      FederationBatchInfo batchInfo = new FederationBatchInfo(batchTag1, date, UNPROCESSED, EFGS);
      when(batchInfoService.findByStatus(UNPROCESSED, EFGS)).thenReturn(list(batchInfo));

      ByteString keyData = FederationBatchTestHelper.createByteStringOfLength(invalidKeyDataLength);
      DiagnosisKey invalidKey = FederationBatchTestHelper.createFederationDiagnosisKeyWithKeyData(keyData);
      DiagnosisKeyBatch batch = FederationBatchTestHelper.createDiagnosisKeyBatch(List.of(invalidKey));
      BatchDownloadResponse downloadResponse = FederationBatchTestHelper.createBatchDownloadResponse(batchTag1,
          Optional.empty(), batch);

      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(downloadResponse);
      batchProcessor.processUnprocessedFederationBatches();

      verifyProcessedWithStatus(batchInfo, PROCESSED_WITH_ERROR);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 9, Integer.MAX_VALUE})
    void testInvalidTRLTriggersNormalization(int invalidTrl) throws Exception {
      FederationBatchInfo batchInfo = new FederationBatchInfo(batchTag1, date, UNPROCESSED, EFGS);
      when(batchInfoService.findByStatus(UNPROCESSED, EFGS)).thenReturn(list(batchInfo));

      DiagnosisKey invalidKey = FederationBatchTestHelper.createBuilderForValidFederationDiagnosisKey()
          .setTransmissionRiskLevel(invalidTrl).build();

      DiagnosisKeyBatch batch = FederationBatchTestHelper.createDiagnosisKeyBatch(List.of(invalidKey));
      BatchDownloadResponse downloadResponse = FederationBatchTestHelper.createBatchDownloadResponse(batchTag1,
          Optional.empty(), batch);

      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(downloadResponse);
      batchProcessor.processUnprocessedFederationBatches();

      ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
      verify(batchInfoService, times(1)).findByStatus(UNPROCESSED, EFGS);
      verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      verify(batchInfoService, times(1)).updateStatus(batchInfo, PROCESSED);
      verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(captor.capture());
      assertThat(captor.getValue()).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 145, 144000})
    void testFailureInvalidRollingPeriod(int invalidRollingPeriod) throws Exception {
      FederationBatchInfo batchInfo = new FederationBatchInfo(batchTag1, date, UNPROCESSED, EFGS);
      when(batchInfoService.findByStatus(UNPROCESSED, EFGS)).thenReturn(list(batchInfo));

      DiagnosisKey invalidKey = FederationBatchTestHelper.createBuilderForValidFederationDiagnosisKey()
          .setRollingPeriod(invalidRollingPeriod).build();
      DiagnosisKeyBatch batch = FederationBatchTestHelper.createDiagnosisKeyBatch(List.of(invalidKey));
      BatchDownloadResponse downloadResponse = FederationBatchTestHelper.createBatchDownloadResponse(batchTag1,
          Optional.empty(), batch);

      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(downloadResponse);
      batchProcessor.processUnprocessedFederationBatches();

      verifyProcessedWithStatus(batchInfo, PROCESSED_WITH_ERROR);
    }
  }

  @Test
  void testProcessBachAndReturnNextBatchIdNotAuthenticated() throws Exception {
    FederationBatchInfo batchInfo = new FederationBatchInfo(batchTag1, date, UNPROCESSED, EFGS);
    when(batchInfoService.findByStatus(UNPROCESSED, EFGS)).thenReturn(list(batchInfo));
    when(federationGatewayDownloadService.downloadBatch(batchTag1, date))
        .thenThrow(FatalFederationGatewayException.class);

    assertThatThrownBy(() -> batchProcessor.processUnprocessedFederationBatches())
        .isExactlyInstanceOf(FatalFederationGatewayException.class);
  }

  public void verifyProcessedWithStatus(FederationBatchInfo federationBatchInfo, FederationBatchStatus expectedStatus)
      throws Exception {
    ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
    verify(batchInfoService, times(1)).findByStatus(UNPROCESSED, EFGS);
    verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
    verify(batchInfoService, times(1)).updateStatus(federationBatchInfo, expectedStatus);
    verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(captor.capture());
    assertThat(captor.getValue()).isEmpty();
  }
}
