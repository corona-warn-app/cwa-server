package app.coronawarn.server.services.federation.upload.payload;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import com.google.protobuf.ByteString;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DiagnosisKeyBatchAssembler {

  private app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey convertKey(
      DiagnosisKey key) {
    return app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey.newBuilder()
        .setKeyData(ByteString.copyFrom(key.getKeyData()))
        .addAllVisitedCountries(key.getVisitedCountries())
        .setRollingPeriod(key.getRollingPeriod())
        .setReportType(key.getReportType())
        .setTransmissionRiskLevel(key.getTransmissionRiskLevel())
        .setOrigin(key.getOriginCountry())
        .build();
  }

  /**
   * Converts persisted keys into Federation Gateway compatible Diagnosis Keys as specified in the protobuf spec.
   * If data can be uploaded with a single request, a list with a single {@link DiagnosisKeyBatch} is returned.
   * @param diagnosisKeys raw list of {@link DiagnosisKey} to be assembled in batches.
   * @return List of {@link DiagnosisKeyBatch} to be uploaded.
   */
  public List<DiagnosisKeyBatch> assembleDiagnosisKeyBatch(List<DiagnosisKey> diagnosisKeys) {
    if (diagnosisKeys.isEmpty()) {
      return Collections.emptyList();
    }
    return List.of(DiagnosisKeyBatch.newBuilder()
        .addAllKeys(diagnosisKeys.stream()
            .filter(DiagnosisKey::isConsentToFederation)
            .map(this::convertKey)
            .collect(Collectors.toList()))
        .build());
  }

}
