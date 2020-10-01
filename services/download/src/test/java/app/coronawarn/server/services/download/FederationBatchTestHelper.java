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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.download.normalization.FederationKeyNormalizer;
import com.google.protobuf.ByteString;
import java.util.Optional;

public class FederationBatchTestHelper {

  public static DiagnosisKeyBatch createDiagnosisKeyBatch(String keyData) {
    return DiagnosisKeyBatch.newBuilder()
        .addKeys(createFederationDiagnosisKey(keyData, 0)).build();
  }

  public static DiagnosisKey createFederationDiagnosisKey(String keyData) {
    return createFederationDiagnosisKey(keyData, 0);
  }

  public static DiagnosisKey createSelfReportedFederationDiagnosisKey(ReportType reportType) {
    return DiagnosisKey.newBuilder()
        .setKeyData(ByteString.copyFromUtf8("test-keyData"))
        .setReportType(reportType)
        .build();
  }

  public static DiagnosisKey createFederationDiagnosisKeyWithoutDaysSinceSymptoms(String keyData) {
    return DiagnosisKey.newBuilder()
        .setKeyData(ByteString.copyFromUtf8(keyData))
        .addVisitedCountries("DE")
        .setRollingStartIntervalNumber(1596153600 / 600)
        .setRollingPeriod(144)
        .setTransmissionRiskLevel(8)
        .build();
  }

  public static DiagnosisKey createFederationDiagnosisKey(String keyData, int daysSinceOnsetOfSymptoms) {
    return DiagnosisKey.newBuilder()
        .setKeyData(ByteString.copyFromUtf8(keyData))
        .addVisitedCountries("DE")
        .setRollingStartIntervalNumber(1596153600 / 600)
        .setRollingPeriod(144)
        .setTransmissionRiskLevel(8)
        .setDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .build();
  }

  public static app.coronawarn.server.common.persistence.domain.DiagnosisKey createDiagnosisKey(String keyData,
      DownloadServiceConfig downloadServiceConfig) {
    return app.coronawarn.server.common.persistence.domain.DiagnosisKey.builder()
        .fromFederationDiagnosisKey(FederationBatchTestHelper.createFederationDiagnosisKey(keyData))
        .withFieldNormalization(new FederationKeyNormalizer(downloadServiceConfig))
        .build();
  }

  public static BatchDownloadResponse createBatchDownloadResponse(String batchTag,
      Optional<String> nextBatchTag) {
    BatchDownloadResponse gatewayResponse = mock(BatchDownloadResponse.class);
    when(gatewayResponse.getBatchTag()).thenReturn(batchTag);
    when(gatewayResponse.getNextBatchTag()).thenReturn(nextBatchTag);
    when(gatewayResponse.getDiagnosisKeyBatch()).thenReturn(Optional.of(createDiagnosisKeyBatch("0123456789ABCDEF")));
    return gatewayResponse;
  }
}
