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

import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.federation.upload.payload.signing.BatchSigner;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PayloadFactory {

  private static final Logger logger = LoggerFactory
      .getLogger(PayloadFactory.class);

  private final DiagnosisKeyBatchAssembler assembler;
  private final BatchSigner signer;

  public PayloadFactory(DiagnosisKeyBatchAssembler assembler, BatchSigner signer) {
    this.assembler = assembler;
    this.signer = signer;
  }

  private static List<app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey>
        sortBatchByKeyData(DiagnosisKeyBatch batch) {
    return batch.getKeysList()
        .stream()
        .sorted(Comparator.comparing(diagnosisKey -> diagnosisKey.getKeyData().toStringUtf8()))
        .collect(Collectors.toList());
  }

  private UploadPayload mapToPayloadAndSign(Integer batchCounter, DiagnosisKeyBatch batch,
      List<FederationUploadKey> originalKeys) {
    var payload = new UploadPayload();
    payload.setBatch(batch);
    payload.setBatchTag(this.generateBatchTag(batchCounter));
    payload.setOriginalKeys(originalKeys);
    try {
      var orderedDiagnosisKeys = sortBatchByKeyData(batch);
      payload.setOrderedKeys(orderedDiagnosisKeys);
      payload.setBatchSignature(signer.createSignatureBytes(batch, orderedDiagnosisKeys));
    } catch (GeneralSecurityException | OperatorCreationException | IOException | CMSException e) {
      logger.error("Failed to generate upload payload signature", e);
    }
    return payload;
  }

  private String generateBatchTag(int counter) {
    var currentTime = LocalDateTime.now(ZoneOffset.UTC);
    return String.format("%d-%d-%d_%dh-%d",
        currentTime.getYear(),
        currentTime.getMonth().getValue(),
        currentTime.getDayOfMonth(),
        currentTime.getHour(),
        counter);
  }

  /**
   * Generates the Payload objects based on a list of Diagnosis Keys. This method will generate batches, add a proper
   * batch tag and sign them with the server private key.
   *
   * @param diagnosisKeys List of Diagnosis Keys.
   * @return upload payload object {@link UploadPayload}.
   */
  public List<UploadPayload> makePayloadList(List<FederationUploadKey> diagnosisKeys) {
    Map<DiagnosisKeyBatch, List<FederationUploadKey>> batchesAndOriginalKeys = assembler
        .assembleDiagnosisKeyBatch(diagnosisKeys);
    AtomicInteger batchCounter = new AtomicInteger(0);

    return batchesAndOriginalKeys.entrySet().stream()
        .map(entry -> this.mapToPayloadAndSign(batchCounter.incrementAndGet(), entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }
}
