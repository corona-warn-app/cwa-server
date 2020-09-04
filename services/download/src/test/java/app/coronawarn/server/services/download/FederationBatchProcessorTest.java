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
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.download.download.FederationBatchDownloader;
import app.coronawarn.server.services.download.download.FederationBatchProcessor;
import app.coronawarn.server.common.federation.client.download.BatchDownloadResponse;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FederationBatchProcessorTest {

  private final LocalDate date = LocalDate.of(2020, 9, 1);
  private final String batchTag1 = "507f191e810c19729de860ea";
  private final String batchTag2 = "507f191e810c19729de860eb";

  private FederationBatchInfoService batchInfoService;
  private DiagnosisKeyService diagnosisKeyService;
  private FederationBatchDownloader batchDownloader;
  private FederationBatchProcessor batchProcessor;

  @BeforeEach
  void setUpBatchProcessor() {
    batchInfoService = spy(mock(FederationBatchInfoService.class));
    diagnosisKeyService = spy(mock(DiagnosisKeyService.class));
    batchDownloader = spy(mock(FederationBatchDownloader.class));
    batchProcessor = new FederationBatchProcessor(batchInfoService, diagnosisKeyService, batchDownloader);
  }

  private DiagnosisKeyBatch buildDiagnosisKeyBatch(List<DiagnosisKey> diagnosisKeys) {
    return DiagnosisKeyBatch.newBuilder().addAllKeys(diagnosisKeys).build();
  }

  @Nested
  @DisplayName("saveFirstBatchInfoForDateTest")
  class SaveFirstBatchInfoForDate {

    @Test
    void testBatchInfoForDateDoesNotExist() {
      when(batchDownloader.downloadFirstBatch(any(LocalDate.class))).thenReturn(Optional.empty());
      batchProcessor.saveFirstBatchInfoForDate(date);
      verify(batchInfoService, never()).save(any(FederationBatchInfo.class));
    }

    @Test
    void testBatchInfoForDateExists() {
      BatchDownloadResponse serverResponse = createFederationGatewayResponse(batchTag1, Optional.empty());
      when(batchDownloader.downloadFirstBatch(eq(date))).thenReturn(Optional.of(serverResponse));

      batchProcessor.saveFirstBatchInfoForDate(date);

      verify(batchInfoService, times(1)).save(eq(new FederationBatchInfo(batchTag1, date)));
    }
  }

  @Nested
  @DisplayName("processUnprocessedFederationBatches")
  class ProcessUnprocessedFederationBatchesTest {

    @Test
    void testNoUnprocessedBatches() {
      when(batchInfoService.findByStatus(any(FederationBatchStatus.class))).thenReturn(emptyList());
      batchProcessor.processUnprocessedFederationBatches();
      verify(batchDownloader, never()).downloadBatch(any(LocalDate.class), anyString());
      verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    }

    @Test
    void testOneUnprocessedBatchNoNextBatch() {
      FederationBatchInfo federationBatchInfo = new FederationBatchInfo(batchTag1, date, UNPROCESSED);

      when(batchInfoService.findByStatus(eq(UNPROCESSED))).thenReturn(singletonList(federationBatchInfo));
      BatchDownloadResponse serverResponse = createFederationGatewayResponse(batchTag1, Optional.empty());
      when(batchDownloader.downloadBatch(date, batchTag1)).thenReturn(Optional.of(serverResponse));

      batchProcessor.processUnprocessedFederationBatches();

      verify(batchInfoService, times(1)).updateStatus(eq(federationBatchInfo), eq(PROCESSED));
      verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(any());
    }

    @Test
    void testOneUnprocessedBatchOneNextBatch() {
      FederationBatchInfo batchInfo1 = new FederationBatchInfo(batchTag1, date, UNPROCESSED);
      FederationBatchInfo batchInfo2 = new FederationBatchInfo(batchTag2, date, UNPROCESSED);
      when(batchInfoService.findByStatus(UNPROCESSED)).thenReturn(Collections.singletonList(batchInfo1));

      BatchDownloadResponse serverResponse1 = createFederationGatewayResponse(batchTag1, Optional.of(batchTag2));
      when(batchDownloader.downloadBatch(date, batchTag1)).thenReturn(Optional.of(serverResponse1));
      BatchDownloadResponse serverResponse2 = createFederationGatewayResponse(batchTag2, Optional.empty());
      when(batchDownloader.downloadBatch(date, batchTag2)).thenReturn(Optional.of(serverResponse2));

      batchProcessor.processUnprocessedFederationBatches();

      verify(batchInfoService, times(1)).findByStatus(UNPROCESSED);

      verify(batchDownloader, times(1)).downloadBatch(date, batchTag1);
      verify(batchInfoService, times(1)).updateStatus(eq(batchInfo1), eq(PROCESSED));

      verify(batchDownloader, times(1)).downloadBatch(date, batchTag2);
      verify(batchInfoService, times(1)).updateStatus(eq(batchInfo2), eq(PROCESSED));

      verify(diagnosisKeyService, times(2)).saveDiagnosisKeys(any());
    }

    @Test
    void testOneUnprocessedBatchFails() {
      when(batchInfoService.findByStatus(UNPROCESSED))
          .thenReturn(singletonList(new FederationBatchInfo(batchTag1, date, UNPROCESSED)));
      when(batchDownloader.downloadBatch(date, batchTag1)).thenReturn(Optional.empty());

      batchProcessor.processUnprocessedFederationBatches();

      verify(batchInfoService, times(1)).findByStatus(UNPROCESSED);
      verify(batchDownloader, times(1)).downloadBatch(eq(date), eq(batchTag1));
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
      verify(batchDownloader, never()).downloadBatch(any(LocalDate.class), anyString());
      verify(batchInfoService, never()).save(any(FederationBatchInfo.class));
    }

    @Test
    void testOneErrorBatchNoNextBatch() {
      when(batchInfoService.findByStatus(ERROR))
          .thenReturn(singletonList(new FederationBatchInfo(batchTag1, date, ERROR)));
      BatchDownloadResponse serverResponse = createFederationGatewayResponse(batchTag1, Optional.empty());
      when(batchDownloader.downloadBatch(date, batchTag1)).thenReturn(Optional.of(serverResponse));

      batchProcessor.processErrorFederationBatches();

      verify(batchInfoService, times(1)).findByStatus(ERROR);
      verify(batchDownloader, times(1)).downloadBatch(any(LocalDate.class), anyString());
      verify(batchInfoService, times(1)).updateStatus(any(FederationBatchInfo.class), eq(PROCESSED));
      verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(any());
    }

    @Test
    void testOneErrorBatchOneNextBatch() {
      FederationBatchInfo batchInfo1 = new FederationBatchInfo(batchTag1, date, ERROR);
      FederationBatchInfo batchInfo2 = new FederationBatchInfo(batchTag2, date, UNPROCESSED);

      when(batchInfoService.findByStatus(ERROR))
          .thenReturn(singletonList(batchInfo1));

      BatchDownloadResponse serverResponse1 = createFederationGatewayResponse(batchTag1, Optional.of(batchTag2));
      when(batchDownloader.downloadBatch(date, batchTag1)).thenReturn(Optional.of(serverResponse1));
      BatchDownloadResponse serverResponse2 = createFederationGatewayResponse(batchTag2, Optional.empty());
      when(batchDownloader.downloadBatch(date, batchTag2)).thenReturn(Optional.of(serverResponse2));

      batchProcessor.processErrorFederationBatches();

      verify(batchInfoService, times(1)).findByStatus(ERROR);
      verify(batchDownloader, times(1)).downloadBatch(eq(date), eq(batchTag1));
      verify(batchInfoService, times(1)).updateStatus(eq(batchInfo1), eq(PROCESSED));

      verify(batchInfoService, times(1)).save(eq(batchInfo2));
      verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(any());
    }

    @Test
    void testOneErrorBatchRetryNotFound() {
      when(batchInfoService.findByStatus(ERROR))
          .thenReturn(singletonList(new FederationBatchInfo(batchTag1, date, ERROR)));
      when(batchDownloader.downloadBatch(date, batchTag1)).thenReturn(Optional.empty());

      batchProcessor.processErrorFederationBatches();

      verify(batchInfoService, times(1)).findByStatus(ERROR);
      verify(batchDownloader, times(1)).downloadBatch(eq(date), eq(batchTag1));
      verify(batchInfoService, times(1)).updateStatus(any(FederationBatchInfo.class), eq(ERROR_WONT_RETRY));
      verify(diagnosisKeyService, never()).saveDiagnosisKeys(any());
    }

    @Test
    void testOneErrorBatchSavingNextBatchInfoFails() {
      when(batchInfoService.findByStatus(ERROR))
          .thenReturn(singletonList(new FederationBatchInfo(batchTag1, date, ERROR)));
      doThrow(RuntimeException.class).when(batchInfoService).save(any(FederationBatchInfo.class));

      BatchDownloadResponse serverResponse = createFederationGatewayResponse(batchTag1, Optional.of(batchTag2));
      when(batchDownloader.downloadBatch(date, batchTag1)).thenReturn(Optional.of(serverResponse));

      batchProcessor.processErrorFederationBatches();

      verify(batchInfoService, times(1)).findByStatus(ERROR);
      verify(batchDownloader, times(1)).downloadBatch(eq(date), eq(batchTag1));
      verify(batchInfoService, times(1)).updateStatus(any(FederationBatchInfo.class), eq(ERROR_WONT_RETRY));
      verify(diagnosisKeyService, times(1)).saveDiagnosisKeys(any());
    }
  }

  private BatchDownloadResponse createFederationGatewayResponse(String batchTag, Optional<String> nextBatchTag) {
    BatchDownloadResponse gatewayResponse = mock(BatchDownloadResponse.class);
    when(gatewayResponse.getBatchTag()).thenReturn(batchTag);
    when(gatewayResponse.getNextBatchTag()).thenReturn(nextBatchTag);
    when(gatewayResponse.getDiagnosisKeyBatch()).thenReturn(buildDiagnosisKeyBatch(emptyList()));
    return gatewayResponse;
  }
}
