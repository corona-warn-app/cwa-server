
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

import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.PROCESSED;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.UNPROCESSED;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
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
import app.coronawarn.server.services.download.download.DiagnosisKeyBatchDownloader;
import app.coronawarn.server.services.download.download.DiagnosisKeyBatchProcessor;
import app.coronawarn.server.services.download.download.FederationGatewayResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DiagnosisKeyBatchProcessorTest {

  LocalDate EXP_DATE = LocalDate.of(2020, 9, 1);
  String EXP_BATCH_TAG = "507f191e810c19729de860ea";

  private FederationBatchInfoService batchInfoService;
  private DiagnosisKeyService diagnosisKeyService;
  private DiagnosisKeyBatchDownloader batchDownloader;
  private DiagnosisKeyBatchProcessor batchProcessor;

  @BeforeEach
  void setUpBatchProcessor() {
    batchInfoService = spy(mock(FederationBatchInfoService.class));
    diagnosisKeyService = spy(mock(DiagnosisKeyService.class));
    batchDownloader = spy(mock(DiagnosisKeyBatchDownloader.class));
    batchProcessor = new DiagnosisKeyBatchProcessor(batchInfoService, diagnosisKeyService, batchDownloader);
  }

  @Nested
  @DisplayName("saveFirstBatchInfoForDateTest")
  class SaveFirstBatchInfoForDate {

    @Test
    void testBatchInfoForDateDoesNotExist() {
      when(batchDownloader.downloadBatch(any(LocalDate.class))).thenReturn(Optional.empty());
      batchProcessor.saveFirstBatchInfoForDate(LocalDate.of(2020, 9, 1));
      verify(batchInfoService, never()).save(any(FederationBatchInfo.class));
    }

    @Test
    void testBatchInfoForDateExists() {
      FederationGatewayResponse serverResponse = mock(FederationGatewayResponse.class);
      when(serverResponse.getBatchTag()).thenReturn(EXP_BATCH_TAG);
      when(batchDownloader.downloadBatch(any(LocalDate.class))).thenReturn(Optional.of(serverResponse));

      batchProcessor.saveFirstBatchInfoForDate(EXP_DATE);

      verify(batchInfoService, times(1)).save(eq(new FederationBatchInfo(EXP_BATCH_TAG, EXP_DATE)));
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
    }

    @Test
    void testOneUnprocessedBatchNoNextBatch() {
      when(batchInfoService.findByStatus(any(FederationBatchStatus.class)))
          .thenReturn(singletonList(new FederationBatchInfo(EXP_BATCH_TAG, EXP_DATE, UNPROCESSED)));
      FederationGatewayResponse serverResponse = mock(FederationGatewayResponse.class);
      when(serverResponse.getNextBatchTag()).thenReturn(Optional.empty());
      when(serverResponse.getDiagnosisKeyBatch()).thenReturn(buildDiagnosisKeyBatch(emptyList()));
      when(batchDownloader.downloadBatch(EXP_DATE, EXP_BATCH_TAG)).thenReturn(Optional.of(serverResponse));

      batchProcessor.processUnprocessedFederationBatches();

      verify(batchInfoService, times(1)).updateStatus(any(FederationBatchInfo.class), eq(PROCESSED));
    }
  }

  // TODO more tests

  private DiagnosisKeyBatch buildDiagnosisKeyBatch(List<DiagnosisKey> diagnosisKeys) {
    return DiagnosisKeyBatch.newBuilder().addAllKeys(diagnosisKeys).build();
  }
}
