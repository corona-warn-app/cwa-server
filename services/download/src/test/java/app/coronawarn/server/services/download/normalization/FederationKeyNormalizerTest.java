package app.coronawarn.server.services.download.normalization;

import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.UNPROCESSED;
import static app.coronawarn.server.services.download.FederationBatchTestHelper.createDiagnosisKey;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static org.assertj.core.util.Lists.list;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.federation.client.download.BatchDownloadResponse;
import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
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
  @Autowired
  FederationBatchProcessor processor;
  @Autowired
  DownloadServiceConfig config;
  @MockBean
  private FederationBatchInfoService batchInfoService;

  @SpyBean
  private DiagnosisKeyService diagnosisKeyService;

  @MockBean
  private FederationGatewayClient federationGatewayClient;

  private static String isoDate(LocalDate date) {
    return date.format(ISO_LOCAL_DATE);
  }

  private Map<String, Pair<Integer, Integer>> getKeysAndDsos() {
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
    String batchTag = "507f191e810c19729de860ea";
    FederationBatchInfo federationBatchInfo = new FederationBatchInfo(batchTag, date, UNPROCESSED);
    when(batchInfoService.findByStatus(UNPROCESSED)).thenReturn(list(federationBatchInfo));
    Optional<BatchDownloadResponse> serverResponse = getBatchDownloadResponse(batchTag);
    when(federationGatewayClient.getDiagnosisKeys(batchTag, isoDate(date))).thenReturn(serverResponse);
    processor.processUnprocessedFederationBatches();

    diagnosisKeyService.getDiagnosisKeys().forEach(dk -> {
      final String keyData = ByteString.copyFrom(dk.getKeyData()).toStringUtf8();
      Assertions.assertEquals(dk.getTransmissionRiskLevel(), getKeysAndDsos().get(keyData).getRight());
    });

  }

  private Optional<BatchDownloadResponse> getBatchDownloadResponse(String batchTag) {
    List<DiagnosisKey> diagnosisKeys = getKeysAndDsos().entrySet()
        .stream()
        .map(e -> createDiagnosisKey(e.getKey(), e.getValue().getLeft()))
        .collect(Collectors.toList());

    DiagnosisKeyBatch diagnosisKeyBatch = DiagnosisKeyBatch.newBuilder()
        .addAllKeys(diagnosisKeys)
        .build();

    return Optional
        .of(new BatchDownloadResponse(diagnosisKeyBatch, batchTag, Optional.empty()));
  }
}
