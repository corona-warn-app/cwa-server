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

import app.coronawarn.server.common.federation.client.download.BatchDownloadResponse;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import com.google.protobuf.ByteString;
import java.util.Optional;

public class FederationBatchTestHelper {

  public static DiagnosisKeyBatch createDiagnosisKeyBatch(String keyData) {
    return DiagnosisKeyBatch.newBuilder()
        .addKeys(createDiagnosisKey(keyData)).build();
  }

  public static DiagnosisKey createDiagnosisKey(String keyData) {
    return
        DiagnosisKey.newBuilder()
            .setKeyData(ByteString.copyFromUtf8(keyData))
            .addVisitedCountries("DE")
            .setRollingStartIntervalNumber(1596153600 / 600)
            .setRollingPeriod(144)
            .setTransmissionRiskLevel(2)
            .build();
  }

  public static DiagnosisKey createDiagnosisKey(String keyData, int daysSinceOnsetOfSymptoms) {
    return
        DiagnosisKey.newBuilder()
            .setKeyData(ByteString.copyFromUtf8(keyData))
            .addVisitedCountries("DE")
            .setRollingStartIntervalNumber(1596153600 / 600)
            .setRollingPeriod(144)
            .setDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
            .build();
  }

  public static Optional<BatchDownloadResponse> createBatchDownloadResponse(String batchTag,
      Optional<String> nextBatchTag) {
    BatchDownloadResponse gatewayResponse = mock(BatchDownloadResponse.class);
    when(gatewayResponse.getBatchTag()).thenReturn(batchTag);
    when(gatewayResponse.getNextBatchTag()).thenReturn(nextBatchTag);
    when(gatewayResponse.getDiagnosisKeyBatch()).thenReturn(createDiagnosisKeyBatch("0123456789ABCDEF"));
    return Optional.of(gatewayResponse);
  }
}
