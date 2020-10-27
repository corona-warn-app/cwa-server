

package app.coronawarn.server.services.download;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey.Builder;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.download.config.DownloadServiceConfig;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.download.normalization.FederationKeyNormalizer;
import com.google.protobuf.ByteString;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class FederationBatchTestHelper {

  private static final String VALID_KEY_DATA = "0123456789ABCDEF";
  private static final String VALID_COUNTRY = "DE";
  private static final int VALID_ROLLING_START_INTERVAL_NUMBER = 1596153600 / 600;
  private static final int VALID_ROLLING_PERIOD = 144;
  private static final int VALID_DSOS = 2;
  private static final int VALID_TRANSMISSION_RISK_LEVEL = 8;
  private static final ReportType VALID_REPORT_TYPE = ReportType.CONFIRMED_TEST;

  public static DiagnosisKeyBatch createDiagnosisKeyBatch(String keyData) {
    return DiagnosisKeyBatch.newBuilder()
        .addKeys(createFederationDiagnosisKeyWithKeyData(keyData)).build();
  }

  public static DiagnosisKeyBatch createDiagnosisKeyBatch(List<DiagnosisKey> diagnosisKeys) {
    return DiagnosisKeyBatch.newBuilder()
        .addAllKeys(diagnosisKeys).build();
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

  public static DiagnosisKey createFederationDiagnosisKeyWithKeyData(String keyData) {
    return createBuilderForValidFederationDiagnosisKey()
        .setKeyData(ByteString.copyFromUtf8(keyData))
        .build();
  }

  public static DiagnosisKey createFederationDiagnosisKeyWithKeyData(ByteString keyData) {
    return createBuilderForValidFederationDiagnosisKey()
        .setKeyData(keyData)
        .build();
  }

  public static DiagnosisKey createFederationDiagnosisKeyWithDsos(int daysSinceOnsetOfSymptoms) {
    return createBuilderForValidFederationDiagnosisKey()
        .setDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .build();
  }

  public static DiagnosisKey createFederationDiagnosisKeyWithReportType(ReportType reportType) {
    return createBuilderForValidFederationDiagnosisKey()
        .setReportType(reportType)
        .build();
  }

  public static ByteString createByteStringOfLength(int length) {
    List<String> bytes = List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F");
    return ByteString.copyFromUtf8(String.valueOf(bytes.get(length % 16)).repeat(Math.max(0, length)));
  }

  public static app.coronawarn.server.common.persistence.domain.DiagnosisKey createDiagnosisKeyForSpecificOriginCountry(
      String keyData, String originCountry,
      DownloadServiceConfig downloadServiceConfig) {
    return app.coronawarn.server.common.persistence.domain.DiagnosisKey.builder()
        .fromFederationDiagnosisKey(FederationBatchTestHelper.createFederationDiagnosisKeyWithKeyData(keyData))
        .withCountryCode(originCountry)
        .withVisitedCountries(Set.of(originCountry))
        .withFieldNormalization(new FederationKeyNormalizer(downloadServiceConfig))
        .build();
  }

  public static app.coronawarn.server.common.persistence.domain.DiagnosisKey createDiagnosisKey(String keyData,
      DownloadServiceConfig downloadServiceConfig) {
    return app.coronawarn.server.common.persistence.domain.DiagnosisKey.builder()
        .fromFederationDiagnosisKey(FederationBatchTestHelper.createFederationDiagnosisKeyWithKeyData(keyData))
        .withFieldNormalization(new FederationKeyNormalizer(downloadServiceConfig))
        .build();
  }

  public static BatchDownloadResponse createBatchDownloadResponse(String batchTag,
      Optional<String> nextBatchTag) {
    return createBatchDownloadResponse(batchTag, nextBatchTag, createDiagnosisKeyBatch("0123456789ABCDEF"));
  }

  public static BatchDownloadResponse createBatchDownloadResponse(String batchTag,
      Optional<String> nextBatchTag, DiagnosisKeyBatch diagnosisKeyBatch) {
    BatchDownloadResponse gatewayResponse = mock(BatchDownloadResponse.class);
    when(gatewayResponse.getBatchTag()).thenReturn(batchTag);
    when(gatewayResponse.getNextBatchTag()).thenReturn(nextBatchTag);
    when(gatewayResponse.getDiagnosisKeyBatch()).thenReturn(Optional.of(diagnosisKeyBatch));
    return gatewayResponse;
  }
}
