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

package app.coronawarn.server.services.federation.upload.payload;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
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

  /**
   * Converts persisted keys into Federation Gateway compatible Diagnosis Keys as specified in the protobuf spec.
   * If data can be uploaded with a single request, a map with a single {@link DiagnosisKeyBatch} entry is returned.
   * @param diagnosisKeys raw list of {@link FederationUploadKey} to be assembled in batches.
   * @return Map containing {@link DiagnosisKeyBatch} and the associated original keys to be uploaded.
   */
  public Map<DiagnosisKeyBatch, List<FederationUploadKey>> assembleDiagnosisKeyBatch(
      List<FederationUploadKey> diagnosisKeys) {
    if (diagnosisKeys.isEmpty()) {
      logger.info("Batches not generated: no pending upload diagnosis keys found.");
      return Collections.emptyMap();
    }
    if (diagnosisKeys.size() < uploadConfig.getMinBatchKeyCount()) {
      logger.info("Batches not generated: less then minimum {} pending upload diagnosis keys.",
          uploadConfig.getMinBatchKeyCount());
      return Collections.emptyMap();
    }
    return partionIntoBatches(diagnosisKeys);
  }

  private Map<DiagnosisKeyBatch, List<FederationUploadKey>> partionIntoBatches(List<FederationUploadKey> keysToUpload) {
    return partitionListBySize(filterByConsent(keysToUpload), uploadConfig.getMaxBatchKeyCount()).stream()
                              .map(partition -> Pair.of(this.makeBatchFromPartition(partition), partition))
                              .collect(Collectors.toMap(pair -> pair.getLeft(), pair -> pair.getRight()));
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
        .filter(DiagnosisKey::isConsentToFederation)
        .collect(Collectors.toList());
  }

  private static Iterable<? extends app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey>
      convertForUpload(List<FederationUploadKey> keys) {
    return keys.stream()
        .map(DiagnosisKeyBatchAssembler::convertKey)
        .collect(Collectors.toList());
  }

  private static app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey convertKey(
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
}
