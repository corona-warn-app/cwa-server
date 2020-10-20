

package app.coronawarn.server.services.download;

import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.ERROR;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.ERROR_WONT_RETRY;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.PROCESSED;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.PROCESSED_WITH_ERROR;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.UNPROCESSED;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.domain.config.TekFieldDerivations;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.services.download.config.DownloadServiceConfig;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.download.validation.ValidFederationKeyFilter;
import com.google.protobuf.ByteString;
import feign.FeignException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = {FederationBatchProcessor.class, FederationBatchInfoService.class, DiagnosisKeyService.class,
    FederationGatewayClient.class, ValidFederationKeyFilter.class, TekFieldDerivations.class})
@DirtiesContext
@EnableConfigurationProperties(value = DownloadServiceConfig.class)
class FederationBatchProcessorTest {

  private final LocalDate date = LocalDate.of(2020, 9, 1);
  private final String batchTag1 = "507f191e810c19729de860ea";
  private final String batchTag2 = "507f191e810c19729de860eb";

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

  @Nested
  @DisplayName("saveFirstBatchInfoForDateTest")
  class SaveFirstBatchInfoForDate {

    @Test
    void testBatchInfoForDateDoesNotExist() {
      doThrow(FederationGatewayException.class).when(federationGatewayDownloadService).downloadBatch(any());
      batchProcessor.saveFirstBatchInfoForDate(date);
      Mockito.verify(batchInfoService, never()).save(any(FederationBatchInfo.class));
    }

    @Test
    void testBatchInfoForDateExists() {
      BatchDownloadResponse serverResponse = FederationBatchTestHelper
          .createBatchDownloadResponse(batchTag1, Optional.empty());
      when(federationGatewayDownloadService.downloadBatch(date)).thenReturn(serverResponse);

      batchProcessor.saveFirstBatchInfoForDate(date);

      Mockito.verify(batchInfoService, times(1)).save(new FederationBatchInfo(batchTag1, date));
    }

    @Test
    void testBatchInfoForDateReturnsNull() {
      when(federationGatewayDownloadService.downloadBatch(date)).thenReturn(null);

      batchProcessor.saveFirstBatchInfoForDate(date);

      Mockito.verify(batchInfoService, never()).save(any());
    }

    @Test
    void testBatchInfoForTodayIsDeleted() {
      LocalDate date = LocalDate.now(ZoneOffset.UTC);
      when(federationGatewayDownloadService.downloadBatch(date)).thenReturn(null);

      batchProcessor.saveFirstBatchInfoForDate(date);

      Mockito.verify(batchInfoService, times(1)).deleteForDate(date);
    }
  }

  @Nested
  @DisplayName("processUnprocessedFederationBatches")
  class ProcessUnprocessedFederationBatchesTest {

    @Test
    void testNoUnprocessedBatches() {
      when(batchInfoService.findByStatus(any(FederationBatchStatus.class))).thenReturn(emptyList());
      batchProcessor.processUnprocessedFederationBatches();
      Mockito.verify(federationGatewayDownloadService, never()).downloadBatch(anyString(), any());
      Mockito.verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    }

    @Test
    void testOneUnprocessedBatchNoNextBatch() {
      FederationBatchInfo federationBatchInfo = new FederationBatchInfo(batchTag1, date, UNPROCESSED);
      when(batchInfoService.findByStatus(UNPROCESSED)).thenReturn(list(federationBatchInfo));
      BatchDownloadResponse serverResponse = FederationBatchTestHelper
          .createBatchDownloadResponse(batchTag1, Optional.empty());
      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(serverResponse);

      batchProcessor.processUnprocessedFederationBatches();

      Mockito.verify(batchInfoService, times(1)).updateStatus(federationBatchInfo, PROCESSED);
      Mockito.verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(any());
    }

    @Test
    void testOneUnprocessedBatchOneNextBatch() {
      FederationBatchInfo batchInfo1 = new FederationBatchInfo(batchTag1, date, UNPROCESSED);
      FederationBatchInfo batchInfo2 = new FederationBatchInfo(batchTag2, date, UNPROCESSED);
      when(batchInfoService.findByStatus(UNPROCESSED)).thenReturn(list(batchInfo1));

      BatchDownloadResponse serverResponse1 = FederationBatchTestHelper
          .createBatchDownloadResponse(batchTag1, Optional.of(batchTag2));
      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(serverResponse1);
      BatchDownloadResponse serverResponse2 = FederationBatchTestHelper
          .createBatchDownloadResponse(batchTag2, Optional.empty());
      when(federationGatewayDownloadService.downloadBatch(batchTag2, date)).thenReturn(serverResponse2);

      batchProcessor.processUnprocessedFederationBatches();

      Mockito.verify(batchInfoService, times(1)).findByStatus(UNPROCESSED);

      Mockito.verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      Mockito.verify(batchInfoService, times(1)).updateStatus(batchInfo1, PROCESSED);

      Mockito.verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag2, date);
      Mockito.verify(batchInfoService, times(1)).updateStatus(batchInfo2, PROCESSED);

      Mockito.verify(diagnosisKeyService, times(2)).saveDiagnosisKeys(any());
    }

    @Test
    void testOneUnprocessedBatchFails() {
      when(batchInfoService.findByStatus(UNPROCESSED))
          .thenReturn(list(new FederationBatchInfo(batchTag1, date, UNPROCESSED)));
      doThrow(FederationGatewayException.class).when(federationGatewayDownloadService).downloadBatch(batchTag1, date);

      batchProcessor.processUnprocessedFederationBatches();

      Mockito.verify(batchInfoService, times(1)).findByStatus(UNPROCESSED);
      Mockito.verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      Mockito.verify(batchInfoService, times(1)).updateStatus(any(FederationBatchInfo.class), eq(ERROR));
      Mockito.verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    }
  }

  @Nested
  @DisplayName("processErrorFederationBatches")
  class ProcessErrorFederationBatchesTest {

    @Test
    void testNoErrorBatches() {
      when(batchInfoService.findByStatus(any(FederationBatchStatus.class))).thenReturn(emptyList());
      batchProcessor.processErrorFederationBatches();
      Mockito.verify(batchInfoService, times(1)).findByStatus(ERROR);
      Mockito.verify(federationGatewayDownloadService, never()).downloadBatch(anyString(), any());
      Mockito.verify(batchInfoService, never()).save(any(FederationBatchInfo.class));
    }

    @Test
    void testOneErrorBatchNoNextBatch() {
      when(batchInfoService.findByStatus(ERROR)).thenReturn(list(new FederationBatchInfo(batchTag1, date, ERROR)));
      BatchDownloadResponse serverResponse = FederationBatchTestHelper
          .createBatchDownloadResponse(batchTag1, Optional.empty());
      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(serverResponse);

      batchProcessor.processErrorFederationBatches();

      Mockito.verify(batchInfoService, times(1)).findByStatus(ERROR);
      Mockito.verify(federationGatewayDownloadService, times(1)).downloadBatch(anyString(), any());
      Mockito.verify(batchInfoService, times(1)).updateStatus(any(FederationBatchInfo.class), eq(PROCESSED));
      Mockito.verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(any());
    }

    @Test
    void testOneErrorBatchOneNextBatch() {
      FederationBatchInfo batchInfo1 = new FederationBatchInfo(batchTag1, date, ERROR);
      FederationBatchInfo batchInfo2 = new FederationBatchInfo(batchTag2, date, UNPROCESSED);

      when(batchInfoService.findByStatus(ERROR)).thenReturn(list(batchInfo1));

      BatchDownloadResponse serverResponse1 = FederationBatchTestHelper
          .createBatchDownloadResponse(batchTag1, Optional.of(batchTag2));
      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(serverResponse1);
      BatchDownloadResponse serverResponse2 = FederationBatchTestHelper
          .createBatchDownloadResponse(batchTag2, Optional.empty());
      when(federationGatewayDownloadService.downloadBatch(batchTag2, date)).thenReturn(serverResponse2);

      batchProcessor.processErrorFederationBatches();

      Mockito.verify(batchInfoService, times(1)).findByStatus(ERROR);
      Mockito.verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      Mockito.verify(batchInfoService, times(1)).updateStatus(batchInfo1, PROCESSED);

      Mockito.verify(batchInfoService, times(1)).save(batchInfo2);
      Mockito.verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(any());
    }

    @Test
    void testOneErrorBatchRetryNotFound() {
      when(batchInfoService.findByStatus(ERROR)).thenReturn(list(new FederationBatchInfo(batchTag1, date, ERROR)));
      doThrow(FeignException.NotFound.class).when(federationGatewayDownloadService).downloadBatch(batchTag1, date);

      batchProcessor.processErrorFederationBatches();

      Mockito.verify(batchInfoService, times(1)).findByStatus(ERROR);
      Mockito.verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      Mockito.verify(batchInfoService, times(1)).updateStatus(any(FederationBatchInfo.class), eq(ERROR_WONT_RETRY));
      Mockito.verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    }

    @Test
    void testOneErrorBatchSavingNextBatchInfoFails() {
      when(batchInfoService.findByStatus(ERROR)).thenReturn(list(new FederationBatchInfo(batchTag1, date, ERROR)));
      doThrow(RuntimeException.class).when(batchInfoService).save(any(FederationBatchInfo.class));

      BatchDownloadResponse serverResponse = FederationBatchTestHelper
          .createBatchDownloadResponse(batchTag1, Optional.of(batchTag2));
      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(serverResponse);

      batchProcessor.processErrorFederationBatches();

      Mockito.verify(batchInfoService, times(1)).findByStatus(ERROR);
      Mockito.verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      Mockito.verify(batchInfoService, times(1)).updateStatus(any(FederationBatchInfo.class), eq(ERROR_WONT_RETRY));
      Mockito.verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(any());
    }
  }

  @Nested
  @DisplayName("testKeyValidationInOneBatch")
  class TestKeyValidationInOneBatch {

    @Test
    void testFailureKeysAreSkipped() {
      FederationBatchInfo batchInfo = new FederationBatchInfo(batchTag1, date, UNPROCESSED);
      when(batchInfoService.findByStatus(UNPROCESSED)).thenReturn(list(batchInfo));

      DiagnosisKey validKey = FederationBatchTestHelper.createBuilderForValidFederationDiagnosisKey().build();
      DiagnosisKey invalidKey = FederationBatchTestHelper
          .createFederationDiagnosisKeyWithKeyData(FederationBatchTestHelper.createByteStringOfLength(32));

      DiagnosisKeyBatch batch = FederationBatchTestHelper.createDiagnosisKeyBatch(List.of(validKey, invalidKey));
      BatchDownloadResponse downloadResponse = FederationBatchTestHelper
          .createBatchDownloadResponse(batchTag1, Optional.empty(), batch);

      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(downloadResponse);
      batchProcessor.processUnprocessedFederationBatches();

      Mockito.verify(batchInfoService, times(1)).findByStatus(UNPROCESSED);
      Mockito.verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      Mockito.verify(batchInfoService, times(1)).updateStatus(batchInfo, PROCESSED_WITH_ERROR);
      Mockito.verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(any());
    }

    @Test
    void testDiagnosisKeyPassesDownloadValidationButBuildingFails() {
      FederationBatchInfo batchInfo = new FederationBatchInfo(batchTag1, date, UNPROCESSED);

      when(batchInfoService.findByStatus(UNPROCESSED)).thenReturn(list(batchInfo));

      DiagnosisKey invalidKey = FederationBatchTestHelper.createBuilderForValidFederationDiagnosisKey()
          .setRollingPeriod(-5)
          .build();
      DiagnosisKeyBatch batch = FederationBatchTestHelper.createDiagnosisKeyBatch(List.of(invalidKey));
      BatchDownloadResponse downloadResponse = FederationBatchTestHelper
          .createBatchDownloadResponse(batchTag1, Optional.empty(), batch);

      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(downloadResponse);
      batchProcessor.processUnprocessedFederationBatches();

      verifyProcessedWithStatus(batchInfo, PROCESSED_WITH_ERROR);
    }

    @ParameterizedTest
    @ValueSource(ints = {-15, -17, 4001})
    void testFailureInvalidDSOS(int invalidDsos) {
      FederationBatchInfo batchInfo = new FederationBatchInfo(batchTag1, date, UNPROCESSED);
      when(batchInfoService.findByStatus(UNPROCESSED)).thenReturn(list(batchInfo));

      DiagnosisKey invalidKey = FederationBatchTestHelper.createBuilderForValidFederationDiagnosisKey()
          .setDaysSinceOnsetOfSymptoms(invalidDsos)
          .build();
      DiagnosisKeyBatch batch = FederationBatchTestHelper.createDiagnosisKeyBatch(List.of(invalidKey));
      BatchDownloadResponse downloadResponse = FederationBatchTestHelper
          .createBatchDownloadResponse(batchTag1, Optional.empty(), batch);

      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(downloadResponse);
      batchProcessor.processUnprocessedFederationBatches();

      verifyProcessedWithStatus(batchInfo, PROCESSED_WITH_ERROR);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 15, 17, 20})
    void testFailureInvalidKeyDataLength(int invalidKeyDataLength) {
      FederationBatchInfo batchInfo = new FederationBatchInfo(batchTag1, date, UNPROCESSED);
      when(batchInfoService.findByStatus(UNPROCESSED)).thenReturn(list(batchInfo));

      ByteString keyData = FederationBatchTestHelper.createByteStringOfLength(invalidKeyDataLength);
      DiagnosisKey invalidKey = FederationBatchTestHelper.createFederationDiagnosisKeyWithKeyData(keyData);
      DiagnosisKeyBatch batch = FederationBatchTestHelper.createDiagnosisKeyBatch(List.of(invalidKey));
      BatchDownloadResponse downloadResponse = FederationBatchTestHelper
          .createBatchDownloadResponse(batchTag1, Optional.empty(), batch);

      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(downloadResponse);
      batchProcessor.processUnprocessedFederationBatches();

      verifyProcessedWithStatus(batchInfo, PROCESSED_WITH_ERROR);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 9, Integer.MAX_VALUE})
    void testInvalidTRLTriggersNormalization(int invalidTrl) {
      FederationBatchInfo batchInfo = new FederationBatchInfo(batchTag1, date, UNPROCESSED);
      when(batchInfoService.findByStatus(UNPROCESSED)).thenReturn(list(batchInfo));

      DiagnosisKey invalidKey = FederationBatchTestHelper.createBuilderForValidFederationDiagnosisKey()
          .setTransmissionRiskLevel(invalidTrl)
          .build();

      DiagnosisKeyBatch batch = FederationBatchTestHelper.createDiagnosisKeyBatch(List.of(invalidKey));
      BatchDownloadResponse downloadResponse = FederationBatchTestHelper
          .createBatchDownloadResponse(batchTag1, Optional.empty(), batch);

      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(downloadResponse);
      batchProcessor.processUnprocessedFederationBatches();

      ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
      Mockito.verify(batchInfoService, times(1)).findByStatus(UNPROCESSED);
      Mockito.verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      Mockito.verify(batchInfoService, times(1)).updateStatus(batchInfo, PROCESSED);
      Mockito.verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(captor.capture());
      assertThat(captor.getValue()).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 145, 144000})
    void testFailureInvalidRollingPeriod(int invalidRollingPeriod) {
      FederationBatchInfo batchInfo = new FederationBatchInfo(batchTag1, date, UNPROCESSED);
      when(batchInfoService.findByStatus(UNPROCESSED)).thenReturn(list(batchInfo));

      DiagnosisKey invalidKey = FederationBatchTestHelper
          .createBuilderForValidFederationDiagnosisKey()
          .setRollingPeriod(invalidRollingPeriod)
          .build();
      DiagnosisKeyBatch batch = FederationBatchTestHelper.createDiagnosisKeyBatch(List.of(invalidKey));
      BatchDownloadResponse downloadResponse = FederationBatchTestHelper
          .createBatchDownloadResponse(batchTag1, Optional.empty(), batch);

      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(downloadResponse);
      batchProcessor.processUnprocessedFederationBatches();

      verifyProcessedWithStatus(batchInfo, PROCESSED_WITH_ERROR);
    }
  }

  public void verifyProcessedWithStatus(FederationBatchInfo federationBatchInfo,
      FederationBatchStatus expectedStatus) {
    ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
    Mockito.verify(batchInfoService, times(1)).findByStatus(UNPROCESSED);
    Mockito.verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
    Mockito.verify(batchInfoService, times(1)).updateStatus(federationBatchInfo, expectedStatus);
    Mockito.verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(captor.capture());
    assertThat(captor.getValue()).isEmpty();
  }
}
