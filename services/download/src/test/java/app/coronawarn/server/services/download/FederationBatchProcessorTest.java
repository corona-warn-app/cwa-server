/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.download;

import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.ERROR;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.ERROR_WONT_RETRY;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.PROCESSED;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.UNPROCESSED;
import static java.util.Collections.emptyList;
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
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.services.download.DownloadServiceConfig.TekFieldDerivations;
import feign.FeignException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(classes = {FederationBatchProcessor.class, FederationBatchInfoService.class, DiagnosisKeyService.class,
    FederationGatewayClient.class, DownloadServiceConfig.class})
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

  @MockBean
  DownloadServiceConfig config;

  @BeforeEach
  void setUp() {
    final TekFieldDerivations tekDerivations = new TekFieldDerivations();
    tekDerivations.setTrlFromDsos(Map.of(1, 1, 2, 2, 3, 3));
    when(config.getTekFieldDerivations()).thenReturn(tekDerivations);
  }

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
      verify(batchInfoService, never()).save(any(FederationBatchInfo.class));
    }

    @Test
    void testBatchInfoForDateExists() {
      BatchDownloadResponse serverResponse = FederationBatchTestHelper
          .createBatchDownloadResponse(batchTag1, Optional.empty());
      when(federationGatewayDownloadService.downloadBatch(date)).thenReturn(serverResponse);

      batchProcessor.saveFirstBatchInfoForDate(date);

      verify(batchInfoService, times(1)).save(new FederationBatchInfo(batchTag1, date));
    }

    @Test
    void testBatchInfoForDateReturnsNull() {
      when(federationGatewayDownloadService.downloadBatch(date)).thenReturn(null);

      batchProcessor.saveFirstBatchInfoForDate(date);

      verify(batchInfoService, never()).save(any());
    }
  }

  @Nested
  @DisplayName("processUnprocessedFederationBatches")
  class ProcessUnprocessedFederationBatchesTest {

    @Test
    void testNoUnprocessedBatches() {
      when(batchInfoService.findByStatus(any(FederationBatchStatus.class))).thenReturn(emptyList());
      batchProcessor.processUnprocessedFederationBatches();
      verify(federationGatewayDownloadService, never()).downloadBatch(anyString(), any());
      verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    }

    @Test
    void testOneUnprocessedBatchNoNextBatch() {
      FederationBatchInfo federationBatchInfo = new FederationBatchInfo(batchTag1, date, UNPROCESSED);
      when(batchInfoService.findByStatus(UNPROCESSED)).thenReturn(list(federationBatchInfo));
      BatchDownloadResponse serverResponse = FederationBatchTestHelper
          .createBatchDownloadResponse(batchTag1, Optional.empty());
      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(serverResponse);

      batchProcessor.processUnprocessedFederationBatches();

      verify(batchInfoService, times(1)).updateStatus(federationBatchInfo, PROCESSED);
      verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(any());
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

      verify(batchInfoService, times(1)).findByStatus(UNPROCESSED);

      verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      verify(batchInfoService, times(1)).updateStatus(batchInfo1, PROCESSED);

      verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag2, date);
      verify(batchInfoService, times(1)).updateStatus(batchInfo2, PROCESSED);

      verify(diagnosisKeyService, times(2)).saveDiagnosisKeys(any());
    }

    @Test
    void testOneUnprocessedBatchFails() {
      when(batchInfoService.findByStatus(UNPROCESSED))
          .thenReturn(list(new FederationBatchInfo(batchTag1, date, UNPROCESSED)));
      doThrow(FederationGatewayException.class).when(federationGatewayDownloadService).downloadBatch(batchTag1, date);

      batchProcessor.processUnprocessedFederationBatches();

      verify(batchInfoService, times(1)).findByStatus(UNPROCESSED);
      verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      verify(batchInfoService, times(1)).updateStatus(any(FederationBatchInfo.class), eq(ERROR));
      verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    }
  }

  @Nested
  @DisplayName("processErrorFederationBatches")
  class ProcessErrorFederationBatchesTest {

    @Test
    void testNoErrorBatches() {
      when(batchInfoService.findByStatus(any(FederationBatchStatus.class))).thenReturn(emptyList());
      batchProcessor.processErrorFederationBatches();
      verify(batchInfoService, times(1)).findByStatus(ERROR);
      verify(federationGatewayDownloadService, never()).downloadBatch(anyString(), any());
      verify(batchInfoService, never()).save(any(FederationBatchInfo.class));
    }

    @Test
    void testOneErrorBatchNoNextBatch() {
      when(batchInfoService.findByStatus(ERROR)).thenReturn(list(new FederationBatchInfo(batchTag1, date, ERROR)));
      BatchDownloadResponse serverResponse = FederationBatchTestHelper
          .createBatchDownloadResponse(batchTag1, Optional.empty());
      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(serverResponse);

      batchProcessor.processErrorFederationBatches();

      verify(batchInfoService, times(1)).findByStatus(ERROR);
      verify(federationGatewayDownloadService, times(1)).downloadBatch(anyString(), any());
      verify(batchInfoService, times(1)).updateStatus(any(FederationBatchInfo.class), eq(PROCESSED));
      verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(any());
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

      verify(batchInfoService, times(1)).findByStatus(ERROR);
      verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      verify(batchInfoService, times(1)).updateStatus(batchInfo1, PROCESSED);

      verify(batchInfoService, times(1)).save(batchInfo2);
      verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(any());
    }

    @Test
    void testOneErrorBatchRetryNotFound() {
      when(batchInfoService.findByStatus(ERROR)).thenReturn(list(new FederationBatchInfo(batchTag1, date, ERROR)));
      doThrow(FeignException.NotFound.class).when(federationGatewayDownloadService).downloadBatch(batchTag1, date);

      batchProcessor.processErrorFederationBatches();

      verify(batchInfoService, times(1)).findByStatus(ERROR);
      verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      verify(batchInfoService, times(1)).updateStatus(any(FederationBatchInfo.class), eq(ERROR_WONT_RETRY));
      verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    }

    @Test
    void testOneErrorBatchSavingNextBatchInfoFails() {
      when(batchInfoService.findByStatus(ERROR)).thenReturn(list(new FederationBatchInfo(batchTag1, date, ERROR)));
      doThrow(RuntimeException.class).when(batchInfoService).save(any(FederationBatchInfo.class));

      BatchDownloadResponse serverResponse = FederationBatchTestHelper
          .createBatchDownloadResponse(batchTag1, Optional.of(batchTag2));
      when(federationGatewayDownloadService.downloadBatch(batchTag1, date)).thenReturn(serverResponse);

      batchProcessor.processErrorFederationBatches();

      verify(batchInfoService, times(1)).findByStatus(ERROR);
      verify(federationGatewayDownloadService, times(1)).downloadBatch(batchTag1, date);
      verify(batchInfoService, times(1)).updateStatus(any(FederationBatchInfo.class), eq(ERROR_WONT_RETRY));
      verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(any());
    }
  }
}
