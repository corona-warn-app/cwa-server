

package app.coronawarn.server.services.federation.upload.payload;

import static app.coronawarn.server.services.federation.upload.UploadLogMessages.BATCHES_NOT_GENERATED_NOT_MINIMUM_PENDING_UPLOAD_DIAGNOSIS_KEYS;
import static app.coronawarn.server.services.federation.upload.UploadLogMessages.BATCHES_NOT_GENERATED_NO_PENDING_UPLOAD_DIAGNOSIS_KEYS;

import app.coronawarn.server.common.Logger;
import app.coronawarn.server.common.LoggerFactory;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

@Component
public class DiagnosisKeyBatchAssembler {

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyBatchAssembler.class);

  private final UploadServiceConfig uploadConfig;
  private final AllowedPropertiesMap allowedPropertiesMap;

  public DiagnosisKeyBatchAssembler(UploadServiceConfig uploadConfig,
      AllowedPropertiesMap allowedPropertiesMap) {
    this.uploadConfig = uploadConfig;
    this.allowedPropertiesMap = allowedPropertiesMap;
  }

  /**
   * Converts persisted keys into Federation Gateway compatible Diagnosis Keys as specified in the protobuf spec.
   * If data can be uploaded with a single request, a map with a single {@link DiagnosisKeyBatch} entry is returned.
   * @param diagnosisKeys raw list of {@link FederationUploadKey} to be assembled in batches.
   * @return Map containing {@link DiagnosisKeyBatch} and the associated original keys to be uploaded.
   */
  public Map<DiagnosisKeyBatch, List<FederationUploadKey>> assembleDiagnosisKeyBatch(
      List<FederationUploadKey> diagnosisKeys) {
    if (diagnosisKeys.isEmpty()) {
      logger.info(BATCHES_NOT_GENERATED_NO_PENDING_UPLOAD_DIAGNOSIS_KEYS);
      return Collections.emptyMap();
    }
    if (diagnosisKeys.size() < uploadConfig.getMinBatchKeyCount()) {
      logger.info(BATCHES_NOT_GENERATED_NOT_MINIMUM_PENDING_UPLOAD_DIAGNOSIS_KEYS,
          uploadConfig.getMinBatchKeyCount());
      return Collections.emptyMap();
    }
    return partitionIntoBatches(diagnosisKeys);
  }

  private Map<DiagnosisKeyBatch, List<FederationUploadKey>> partitionIntoBatches(
      List<FederationUploadKey> keysToUpload) {
    return partitionListBySize(filterByConsent(keysToUpload), uploadConfig.getMaxBatchKeyCount()).stream()
        .map(partition -> Pair.of(makeBatchFromPartition(partition), partition))
        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
  }

  private DiagnosisKeyBatch makeBatchFromPartition(List<FederationUploadKey> paritionOfKeys) {
    return DiagnosisKeyBatch.newBuilder().addAllKeys(convertForUpload(paritionOfKeys)).build();
  }

  private <T> List<List<T>> partitionListBySize(List<T> keysToUpload, int size) {
    ListIterator<T> iterator = keysToUpload.listIterator();
    List<List<T>> partitions = new ArrayList<>();

    ArrayList<T> currentPartition = new ArrayList<>(size / 2);
    while (iterator.hasNext()) {
      currentPartition = newPartitionIfThresholdMet(partitions, currentPartition, size);
      currentPartition.add(iterator.next());
    }
    partitions.add(currentPartition);
    return partitions;
  }

  private <T> ArrayList<T> newPartitionIfThresholdMet(List<List<T>> partitions, ArrayList<T> currentPartition,
      int size) {
    if (currentPartition.size() == size) {
      partitions.add(currentPartition); // Add this to the set when done with it
      currentPartition = new ArrayList<>();
    }
    return currentPartition;
  }

  private List<FederationUploadKey> filterByConsent(List<FederationUploadKey> diagnosisKeys) {
    return diagnosisKeys.stream()
        .filter(FederationUploadKey::isConsentToFederation)
        .collect(Collectors.toList());
  }

  private Iterable<? extends app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey>
      convertForUpload(List<FederationUploadKey> keys) {
    return keys.stream()
        .map(this::convertKey)
        .collect(Collectors.toList());
  }

  private DiagnosisKey convertKey(
      FederationUploadKey key) {
    return app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey.newBuilder()
        .setKeyData(ByteString.copyFrom(key.getKeyData()))
        .setRollingStartIntervalNumber(key.getRollingStartIntervalNumber())
        .setRollingPeriod(key.getRollingPeriod())
        .setTransmissionRiskLevel(key.getTransmissionRiskLevel())
        .addAllVisitedCountries(key.getVisitedCountries())
        .setOrigin(key.getOriginCountry())
        .setReportType(allowedPropertiesMap.getReportTypeOrDefault(key.getReportType()))
        .setDaysSinceOnsetOfSymptoms(allowedPropertiesMap.getDsosOrDefault(key.getDaysSinceOnsetOfSymptoms()))
        .build();
  }
}
