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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.federation.client.download.BatchDownloadResponse;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.services.download.DownloadServiceConfig;
import app.coronawarn.server.services.download.FederationBatchProcessor;
import app.coronawarn.server.services.download.FederationBatchUtils;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

class DownloadTest {

  private FederationBatchProcessor federationBatchProcessor;
  private Download download;
  private DownloadServiceConfig serviceConfig;

  @BeforeEach
  void setUpBatchProcessor() {
    this.serviceConfig = new DownloadServiceConfig();
    serviceConfig.setEfgsOffsetDays(1);
    federationBatchProcessor = spy(mock(FederationBatchProcessor.class));
    download = new Download(federationBatchProcessor, serviceConfig);
  }

  @Test
  void testRun() {
    download.run(null);

    verify(federationBatchProcessor, times(1)).saveFirstBatchInfoForDate(any(LocalDate.class));
    verify(federationBatchProcessor, times(1)).processErrorFederationBatches();
    verify(federationBatchProcessor, times(1)).processUnprocessedFederationBatches();
  }

  @Nested
  @SpringBootTest
  class DownloadInSpringContextTest {

    @MockBean
    private FederationGatewayClient federationGatewayClient;

    @MockBean
    private FederationBatchInfoService federationBatchInfoService;

    @Test
    @DirtiesContext
    void testRunSuccessfully() {
      BatchDownloadResponse serverResponse = FederationBatchUtils.createBatchDownloadResponse("abc", Optional.empty());
      when(federationGatewayClient.getDiagnosisKeys(anyString())).thenReturn(serverResponse);

      verify(federationGatewayClient, times(1)).getDiagnosisKeys(anyString());
      verify(federationGatewayClient, never()).getDiagnosisKeys(anyString(), anyString());
    }

    @Test
    @DirtiesContext
    void testExceptionInBatchInfoService() {
      BatchDownloadResponse serverResponse = FederationBatchUtils.createBatchDownloadResponse("abc", Optional.empty());
      when(federationGatewayClient.getDiagnosisKeys(anyString())).thenReturn(serverResponse);
      doThrow(RuntimeException.class).when(federationBatchInfoService).save(any());

      verify(federationGatewayClient, times(1)).getDiagnosisKeys(anyString());
    }
  }
}
