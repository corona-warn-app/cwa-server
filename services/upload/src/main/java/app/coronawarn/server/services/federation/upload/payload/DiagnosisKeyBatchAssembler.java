package app.coronawarn.server.services.federation.upload.payload;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DiagnosisKeyBatchAssembler {

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyBatchAssembler.class);

  private UploadServiceConfig uploadConfig;

  public DiagnosisKeyBatchAssembler(UploadServiceConfig uploadConfig) {
    this.uploadConfig = uploadConfig;
  }

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
      logger.info("Batches not generated: no pending upload diagnosis keys found.");
      return Collections.emptyList();
    }
    if (diagnosisKeys.size() < uploadConfig.getMinBatchKeyCount()) {
      logger.info("Batches not generated: less then minimum {} pending upload diagnosis keys.",
          uploadConfig.getMinBatchKeyCount());
      return Collections.emptyList();
    }

    return partionIntoBatches(filterAndConvertToUploadStructure(diagnosisKeys));
  }

  private List<DiagnosisKeyBatch> partionIntoBatches(
      List<app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey> keysToUpload) {

    return  partitionListBySize(keysToUpload, uploadConfig.getMaxBatchKeyCount()).stream()
                              .map(this::makeBatchFromPartition)
                              .collect(Collectors.toList());
  }

  private DiagnosisKeyBatch makeBatchFromPartition(
      List<app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey> paritionOfKeys) {
    return DiagnosisKeyBatch.newBuilder().addAllKeys(paritionOfKeys).build();
  }

  private <T> List<List<T>> partitionListBySize(List<T> keysToUpload, int size) {
    ListIterator<T> iterator = keysToUpload.listIterator();
    List<List<T>> partitions = new ArrayList<List<T>>();

    ArrayList<T> currentPartition = new ArrayList<T>(size / 2);
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
      currentPartition = new ArrayList<T>();
    }
    return currentPartition;
  }

  private List<app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey>
      filterAndConvertToUploadStructure(List<DiagnosisKey> diagnosisKeys) {
    return diagnosisKeys.stream()
        .filter(DiagnosisKey::isConsentToFederation)
        .map(this::convertKey)
        .collect(Collectors.toList());
  }
}
