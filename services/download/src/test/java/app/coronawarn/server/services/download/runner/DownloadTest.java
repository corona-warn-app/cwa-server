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

package app.coronawarn.server.services.download.runner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import app.coronawarn.server.services.download.download.DiagnosisKeyBatchProcessor;
import app.coronawarn.server.services.download.download.DownloadServiceConfig;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DownloadTest {

  private DiagnosisKeyBatchProcessor diagnosisKeyBatchProcessor;
  private Download download;
  private DownloadServiceConfig serviceConfig;

  @BeforeEach
  void setUpBatchProcessor() {
    this.serviceConfig = new DownloadServiceConfig();
    serviceConfig.setEfgsOffsetDays(1);
    diagnosisKeyBatchProcessor = spy(mock(DiagnosisKeyBatchProcessor.class));
    download = new Download(diagnosisKeyBatchProcessor, serviceConfig);
  }

  @Test
  void testRun() {
    download.run(null);

    verify(diagnosisKeyBatchProcessor, times(1)).saveFirstBatchInfoForDate(any(LocalDate.class));
    verify(diagnosisKeyBatchProcessor, times(1)).processErrorFederationBatches();
    verify(diagnosisKeyBatchProcessor, times(1)).processUnprocessedFederationBatches();
  }
}
