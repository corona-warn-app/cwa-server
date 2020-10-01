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
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey.Builder;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.download.normalization.FederationKeyNormalizer;
import com.google.protobuf.ByteString;
import java.util.List;
import java.util.Optional;

public class FederationBatchTestHelper {

  private static final String VALID_KEY_DATA = "0123456789ABCDEF";
  private static final String VALID_COUNTRY = "DE";
  private static final int VALID_ROLLING_START_INTERVAL_NUMBER = 1596153600 / 600;
  private static final int VALID_ROLLING_PERIOD = 144;
  private static final int VALID_DSOS = 2;
  private static final int VALID_TRANSMISSION_RISK_LEVEL = 8;
  private static final ReportType VALID_REPORT_TYPE = ReportType.CONFIRMED_CLINICAL_DIAGNOSIS;

  public static DiagnosisKeyBatch createDiagnosisKeyBatch(String keyData) {
    return DiagnosisKeyBatch.newBuilder()
        .addKeys(createFederationDiagnosisKeyWithDSOS(keyData, 0)).build();
  }

  public static Builder createBuilderForValidFederationDiagnosisKey() {
    return DiagnosisKey.newBuilder()
        .setKeyData(ByteString.copyFromUtf8(VALID_KEY_DATA))
        .setOrigin(VALID_COUNTRY)
        .addVisitedCountries(VALID_COUNTRY)
        .setRollingStartIntervalNumber(VALID_ROLLING_START_INTERVAL_NUMBER)
        .setRollingPeriod(VALID_ROLLING_PERIOD)
        .setDaysSinceOnsetOfSymptoms(VALID_DSOS)
        .setTransmissionRiskLevel(VALID_TRANSMISSION_RISK_LEVEL)
        .setReportType(VALID_REPORT_TYPE);
  }

  public static DiagnosisKey createFederationDiagnosisKeyWithDSOS(String keyData) {
    return createFederationDiagnosisKeyWithDSOS(keyData, 0);
  }

  public static DiagnosisKey createFederationDiagnosisKeyWithReportType(ReportType reportType) {
    return createBuilderForValidFederationDiagnosisKey()
        .setReportType(reportType)
        .build();
  }

  public static DiagnosisKey createDiagnosisKeyWithKeyDataLength(int length) {
    List<String> bytes = List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F");
    return createBuilderForValidFederationDiagnosisKey()
        .setKeyData(ByteString.copyFromUtf8(String.valueOf(bytes.get(length % 16)).repeat(Math.max(0, length))))
        .build();
  }

  public static DiagnosisKey createFederationDiagnosisKeyWithoutDaysSinceSymptoms() {
    return createBuilderForValidFederationDiagnosisKey()
        .clearDaysSinceOnsetOfSymptoms()
        .build();
  }

  public static DiagnosisKey createFederationDiagnosisKeyWithDSOS(String keyData, int daysSinceOnsetOfSymptoms) {
    return createBuilderForValidFederationDiagnosisKey()
        .setDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .build();
  }

  public static app.coronawarn.server.common.persistence.domain.DiagnosisKey createDiagnosisKey(String keyData,
      DownloadServiceConfig downloadServiceConfig) {
    return app.coronawarn.server.common.persistence.domain.DiagnosisKey.builder()
        .fromFederationDiagnosisKey(FederationBatchTestHelper.createFederationDiagnosisKeyWithDSOS(keyData))
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
