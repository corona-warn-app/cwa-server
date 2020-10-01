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

package app.coronawarn.server.services.download.normalization;

import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.UNPROCESSED;
import static app.coronawarn.server.services.download.FederationBatchTestHelper.createFederationDiagnosisKey;
import static org.assertj.core.util.Lists.list;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.normalization.DiagnosisKeyNormalizer;
import app.coronawarn.server.common.persistence.domain.normalization.NormalizableFields;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.download.BatchDownloadResponse;
import app.coronawarn.server.services.download.config.DownloadServiceConfig;
import app.coronawarn.server.services.download.FederationBatchProcessor;
import app.coronawarn.server.services.download.FederationGatewayDownloadService;
import app.coronawarn.server.services.download.validation.ValidFederationKeyFilter;
import com.google.protobuf.ByteString;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext
class FederationKeyNormalizerTest {

  private static final String BATCH_TAG = "507f191e810c19729de860ea";

  private FederationBatchProcessor processor;
  @Autowired
  private DownloadServiceConfig config;
  @Autowired
  private DiagnosisKeyRepository repository;
  @Autowired
  private ValidFederationKeyFilter validator;
  @SpyBean
  private DiagnosisKeyService diagnosisKeyService;
  @MockBean
  private FederationBatchInfoService batchInfoService;
  @MockBean
  private FederationGatewayDownloadService federationGatewayDownloadService;

  private Map<String, Pair<Integer, Integer>> getKeysAndDsos() {
    return Map.of("0123456789ABCDEX", Pair.of(4, 1),
        "0123456789ABCDEY", Pair.of(3, 3),
        "0123456789ABCDEZ", Pair.of(2, 5),
        "0123456789ABCDED", Pair.of(1, 6),
        "0123456789ABCDEE", Pair.of(0, 8),
        "0123456789ABCDEF", Pair.of(-1, 6),
        "0123456789ABCDEG", Pair.of(-2, 5),
        "0123456789ABCDEH", Pair.of(-3, 3),
        "0123456789ABCDEI", Pair.of(-4, 1)
    );
  }

  @BeforeEach
  void setUp() {
    processor = new FederationBatchProcessor(batchInfoService, diagnosisKeyService, federationGatewayDownloadService,
        config, validator);
    repository.deleteAll();
  }

  @Test
  void testBatchKeysWithDsosAndWithoutTrlAreNormalized() {
    LocalDate date = LocalDate.of(2020, 9, 1);
    FederationBatchInfo federationBatchInfo = new FederationBatchInfo(BATCH_TAG, date, UNPROCESSED);
    when(batchInfoService.findByStatus(UNPROCESSED)).thenReturn(list(federationBatchInfo));
    BatchDownloadResponse serverResponse = getBatchDownloadResponse();
    when(federationGatewayDownloadService.downloadBatch(BATCH_TAG, date)).thenReturn(serverResponse);
    processor.processUnprocessedFederationBatches();
    diagnosisKeyService.getDiagnosisKeys().forEach(dk -> {
      String keyData = ByteString.copyFrom(dk.getKeyData()).toStringUtf8();
      assertEquals(getKeysAndDsos().get(keyData).getRight(), dk.getTransmissionRiskLevel());
    });

  }

  @Test
  void testWhenBatchKeyWithoutDsosShouldThrowException() {
    DiagnosisKeyNormalizer normalizer = new FederationKeyNormalizer(config);
    NormalizableFields nf = NormalizableFields.of(1, null);
    assertThrows(IllegalArgumentException.class, () -> normalizer.normalize(nf));
  }

  private BatchDownloadResponse getBatchDownloadResponse() {
    List<DiagnosisKey> diagnosisKeys = getKeysAndDsos().entrySet()
        .stream()
        .map(e -> createFederationDiagnosisKey(e.getKey(), e.getValue().getLeft()))
        .collect(Collectors.toList());
    DiagnosisKeyBatch diagnosisKeyBatch = DiagnosisKeyBatch.newBuilder()
        .addAllKeys(diagnosisKeys)
        .build();
    return new BatchDownloadResponse(BATCH_TAG, Optional.of(diagnosisKeyBatch), Optional.empty());
  }
}
