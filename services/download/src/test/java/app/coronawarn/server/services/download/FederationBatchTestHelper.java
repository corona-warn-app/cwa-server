

package app.coronawarn.server.services.download;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
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
