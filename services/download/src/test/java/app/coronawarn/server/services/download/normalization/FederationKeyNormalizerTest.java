

package app.coronawarn.server.services.download.normalization;

import static app.coronawarn.server.common.persistence.domain.FederationBatchSourceSystem.EFGS;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.UNPROCESSED;
import static org.assertj.core.util.Lists.list;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.config.TekFieldDerivations;
import app.coronawarn.server.common.persistence.domain.normalization.DiagnosisKeyNormalizer;
import app.coronawarn.server.common.persistence.domain.normalization.NormalizableFields;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.download.BatchDownloadResponse;
import app.coronawarn.server.services.download.FederationBatchProcessor;
import app.coronawarn.server.services.download.FederationBatchTestHelper;
import app.coronawarn.server.services.download.FederationGatewayDownloadService;
import app.coronawarn.server.services.download.config.DownloadServiceConfig;
import app.coronawarn.server.services.download.validation.ValidFederationKeyFilter;
import com.google.protobuf.ByteString;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
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

  private Map<String, Integer> getKeysWithDaysSinceSymptoms() {
    return Map.of("0123456789ABCDEX", 1,
        "0123456789ABCDEY", 3,
        "0123456789ABCDEZ", 2,
        "0123456789ABCDED", 1,
        "0123456789ABCDEE", 0,
        "0123456789ABCDEF", -1,
        "0123456789ABCDEG", -2,
        "0123456789ABCDEH", -3,
        "0123456789ABCDEI", -4
    );
  }

  @BeforeEach
  void setUp() {
    processor = new FederationBatchProcessor(batchInfoService, diagnosisKeyService, federationGatewayDownloadService,
        config, validator);
    repository.deleteAll();
  }

  @Test
  void testBatchKeysWithDsosAndWithoutTrlAreNormalized() throws Exception {
    LocalDate date = LocalDate.of(2020, 9, 1);
    FederationBatchInfo federationBatchInfo = new FederationBatchInfo(BATCH_TAG, date, UNPROCESSED, EFGS);
    when(batchInfoService.findByStatus(UNPROCESSED,EFGS)).thenReturn(list(federationBatchInfo));

    BatchDownloadResponse serverResponse = createBatchDownloadResponseWithKeys(this::createDiagnosisKeyWithNoTrl);
    when(federationGatewayDownloadService.downloadBatch(BATCH_TAG, date)).thenReturn(serverResponse);

    processor.processUnprocessedFederationBatches();
    diagnosisKeyService.getDiagnosisKeys().forEach(dk -> {
      TekFieldDerivations tekDerivationMap = config.getTekFieldDerivations();
      String keyData = ByteString.copyFrom(dk.getKeyData()).toStringUtf8();
      assertEquals(tekDerivationMap
              .deriveTransmissionRiskLevelFromDaysSinceSymptoms(getKeysWithDaysSinceSymptoms().get(keyData)),
          dk.getTransmissionRiskLevel());
    });
  }

  @Test
  void testTrlIsNormalizedWhenValueProvidedIsMaxInt() throws Exception {
    LocalDate date = LocalDate.of(2020, 9, 1);
    FederationBatchInfo federationBatchInfo = new FederationBatchInfo(BATCH_TAG, date, UNPROCESSED, EFGS);
    when(batchInfoService.findByStatus(UNPROCESSED,EFGS)).thenReturn(list(federationBatchInfo));

    BatchDownloadResponse serverResponse = createBatchDownloadResponseWithKeys(this::createDiagnosisKeyWithMaxIntTrl);
    when(federationGatewayDownloadService.downloadBatch(BATCH_TAG, date)).thenReturn(serverResponse);

    processor.processUnprocessedFederationBatches();

    diagnosisKeyService.getDiagnosisKeys().forEach(dk -> {
      TekFieldDerivations tekDerivationMap = config.getTekFieldDerivations();
      String keyData = ByteString.copyFrom(dk.getKeyData()).toStringUtf8();
      assertEquals(tekDerivationMap
              .deriveTransmissionRiskLevelFromDaysSinceSymptoms(getKeysWithDaysSinceSymptoms().get(keyData)),
          dk.getTransmissionRiskLevel());
    });
  }

  @Test
  void testWhenBatchKeyWithoutDsosShouldThrowException() {
    DiagnosisKeyNormalizer normalizer = new FederationKeyNormalizer(config);
    NormalizableFields nf = NormalizableFields.of(1, null);
    assertThrows(IllegalArgumentException.class, () -> normalizer.normalize(nf));
  }

  private BatchDownloadResponse createBatchDownloadResponseWithKeys(
      Function<Entry<String, Integer>, DiagnosisKey> diagnosisKeyFactory) {
    List<DiagnosisKey> diagnosisKeys = getKeysWithDaysSinceSymptoms().entrySet().stream()
        .map(e -> diagnosisKeyFactory.apply(e)).collect(Collectors.toList());
    DiagnosisKeyBatch diagnosisKeyBatch = DiagnosisKeyBatch.newBuilder().addAllKeys(diagnosisKeys).build();
    return new BatchDownloadResponse(BATCH_TAG, Optional.of(diagnosisKeyBatch), Optional.empty());
  }

  private DiagnosisKey createDiagnosisKeyWithNoTrl(Entry<String, Integer> entry) {
    return FederationBatchTestHelper.createBuilderForValidFederationDiagnosisKey()
        .setKeyData(ByteString.copyFromUtf8(entry.getKey()))
        .clearTransmissionRiskLevel()
        .setDaysSinceOnsetOfSymptoms(entry.getValue()).build();
  }

  /**
   * @return A Diagnosis Key which contains TRL set to MAX INT (as per one of EFGS specification).
   * @see https://github.com/eu-federation-gateway-service/efgs-onboarding/blob/master/KeySharingDSOSGuide.md
   */
  private DiagnosisKey createDiagnosisKeyWithMaxIntTrl(Entry<String, Integer> entry) {
    return FederationBatchTestHelper.createBuilderForValidFederationDiagnosisKey()
        .setKeyData(ByteString.copyFromUtf8(entry.getKey()))
        .setTransmissionRiskLevel(Integer.MAX_VALUE)
        .setDaysSinceOnsetOfSymptoms(entry.getValue()).build();
  }
}
