package app.coronawarn.server.services.download.normalization;

import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.UNPROCESSED;
import static app.coronawarn.server.services.download.FederationBatchTestHelper.createDiagnosisKey;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static org.assertj.core.util.Lists.list;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.federation.client.download.BatchDownloadResponse;
import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.normalization.DiagnosisKeyNormalizer;
import app.coronawarn.server.common.persistence.domain.normalization.NormalizableFields;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.download.DownloadServiceConfig;
import app.coronawarn.server.services.download.FederationBatchProcessor;
import com.google.protobuf.ByteString;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

@SpringBootTest
class FederationKeyNormalizerTest {

  private final LocalDate date = LocalDate.of(2020, 9, 1);
  FederationBatchProcessor processor;
  @Autowired
  DownloadServiceConfig config;
  @MockBean
  private FederationBatchInfoService batchInfoService;

  @SpyBean
  private DiagnosisKeyService diagnosisKeyService;

  @MockBean
  private FederationGatewayClient federationGatewayClient;
  private static final String BATCH_TAG = "507f191e810c19729de860ea";

  private static String isoDate(LocalDate date) {
    return date.format(ISO_LOCAL_DATE);
  }

  private static Map<String, Pair<Integer, Integer>> getKeysAndDsos() {
    return Map.of("0123456789ABCDEA", Pair.of(-4, 1),
        "0123456789ABCDEB", Pair.of(-3, 3),
        "0123456789ABCDEC", Pair.of(-2, 5),
        "0123456789ABCDED", Pair.of(-1, 6),
        "0123456789ABCDEE", Pair.of(0, 8),
        "0123456789ABCDEF", Pair.of(1, 6),
        "0123456789ABCDEG", Pair.of(2, 5),
        "0123456789ABCDEH", Pair.of(3, 3),
        "0123456789ABCDEI", Pair.of(4, 1)
    );
  }

  @BeforeEach
  void setUp() {
    processor = new FederationBatchProcessor(batchInfoService, diagnosisKeyService, federationGatewayClient, config);
  }

  @Test
  void testBatchKeysWithDsosAndWithoutTrlAreNormalized() {
    FederationBatchInfo federationBatchInfo = new FederationBatchInfo(BATCH_TAG, date, UNPROCESSED);
    when(batchInfoService.findByStatus(UNPROCESSED)).thenReturn(list(federationBatchInfo));
    Optional<BatchDownloadResponse> serverResponse = getBatchDownloadResponse();
    when(federationGatewayClient.getDiagnosisKeys(BATCH_TAG, isoDate(date))).thenReturn(serverResponse);
    processor.processUnprocessedFederationBatches();
    diagnosisKeyService.getDiagnosisKeys().forEach(dk -> {
      String keyData = ByteString.copyFrom(dk.getKeyData()).toStringUtf8();
      Assertions.assertEquals(dk.getTransmissionRiskLevel(), getKeysAndDsos().get(keyData).getRight());
    });

  }

  @Test
  void testWhenBatchKeyWithoutDsosShouldThrowException() {
    DiagnosisKeyNormalizer normalizer = new FederationKeyNormalizer(config.getTekFieldDerivations().getTrlFromDsos());
    assertThrows(IllegalArgumentException.class, () -> normalizer.normalize(NormalizableFields.of(1, null)));
  }

  private Optional<BatchDownloadResponse> getBatchDownloadResponse() {
    List<DiagnosisKey> diagnosisKeys = getKeysAndDsos().entrySet()
        .stream()
        .map(e -> createDiagnosisKey(e.getKey(), e.getValue().getLeft()))
        .collect(Collectors.toList());

    DiagnosisKeyBatch diagnosisKeyBatch = DiagnosisKeyBatch.newBuilder()
        .addAllKeys(diagnosisKeys)
        .build();

    return Optional
        .of(new BatchDownloadResponse(diagnosisKeyBatch, BATCH_TAG, Optional.empty()));
  }
}
