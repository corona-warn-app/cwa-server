

package app.coronawarn.server.services.federation.upload.payload;

import static app.coronawarn.server.services.federation.upload.utils.MockData.*;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.List;
import java.util.stream.Stream;

class DiagnosisKeyBatchAssemblerTest {

  /**
   * @return A stream of tuples which represents the dataset together with the
   * expectation required to test batch key partitioning.
   */
  static class KeysToPartitionProvider implements ArgumentsProvider {
    public final int minKeyThreshold = 140;
    public final int maxKeyCount = 4000;

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
      return Stream.of(
          Arguments.of(generateRandomUploadKeys(true, minKeyThreshold - 1), 0),
          Arguments.of(generateRandomUploadKeys(true, minKeyThreshold), 1),
          Arguments.of(generateRandomUploadKeys(true, maxKeyCount), 1),
          Arguments.of(generateRandomUploadKeys(true, maxKeyCount / 2), 1),
          Arguments.of(generateRandomUploadKeys(true, maxKeyCount - 1), 1),
          Arguments.of(generateRandomUploadKeys(true, maxKeyCount + 1), 2),
          Arguments.of(generateRandomUploadKeys(true, 2 * maxKeyCount), 2),
          Arguments.of(generateRandomUploadKeys(true, 3 * maxKeyCount), 3),
          Arguments.of(generateRandomUploadKeys(true, 4 * maxKeyCount), 4),
          Arguments.of(generateRandomUploadKeys(true, 2 * maxKeyCount + 1), 3),
          Arguments.of(generateRandomUploadKeys(true, 2 * maxKeyCount + maxKeyCount / 2), 3),
          Arguments.of(generateRandomUploadKeys(true, 2 * maxKeyCount - maxKeyCount / 2), 2)
      );
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @EnableConfigurationProperties(value = UploadServiceConfig.class)
  @ExtendWith(SpringExtension.class)
  @ContextConfiguration(classes = {AllowedPropertiesMap.class,
      DiagnosisKeyBatchAssembler.class}, initializers = ConfigFileApplicationContextInitializer.class)
  class AllPropertiesEnabled {

    public int minKeyThreshold;
    public int maxKeyCount;

    @Autowired
    UploadServiceConfig uploadServiceConfig;

    @Autowired
    DiagnosisKeyBatchAssembler diagnosisKeyBatchAssembler;

    @BeforeAll
    void setup() {
      minKeyThreshold = uploadServiceConfig.getMinBatchKeyCount();
      maxKeyCount = uploadServiceConfig.getMaxBatchKeyCount();
    }

    private void assertKeysAreEqual(DiagnosisKey persistenceKey,
        app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey exportKey) {
      Assertions.assertArrayEquals(persistenceKey.getKeyData(), exportKey.getKeyData().toByteArray(),
          "Key Data should be the same");
      Assertions.assertArrayEquals(persistenceKey.getVisitedCountries().toArray(),
          exportKey.getVisitedCountriesList().toArray(),
          "Visited countries should be the same");
      Assertions.assertEquals(persistenceKey.getRollingPeriod(), exportKey.getRollingPeriod(),
          "Rolling Period should be the same");
      Assertions.assertEquals(persistenceKey.getReportType(), exportKey.getReportType(),
          "Verification Type should be the same");
      Assertions.assertEquals(persistenceKey.getTransmissionRiskLevel(), exportKey.getTransmissionRiskLevel(),
          "Transmission Risk Level should be the same");
      Assertions.assertEquals(persistenceKey.getOriginCountry(), exportKey.getOrigin(),
          "Origin Country should be the same");
    }

    @Test
    void shouldReturnEmptyListIfNoKeysGiven() {
      var result = diagnosisKeyBatchAssembler.assembleDiagnosisKeyBatch(emptyList());
      Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListIfLessThenThresholdKeysGiven() {
      var result = diagnosisKeyBatchAssembler.assembleDiagnosisKeyBatch(generateRandomUploadKeys(true, minKeyThreshold - 1));
      Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void packagedKeysShouldContainInitialInformation() {
      var fakeKeys = generateRandomUploadKeys(true, minKeyThreshold);
      var result = diagnosisKeyBatchAssembler.assembleDiagnosisKeyBatch(fakeKeys);
      var firstBatch = result.keySet().iterator().next();
      Assertions.assertEquals(fakeKeys.size(), firstBatch.getKeysCount());
      // as keys are created equal we need to compare just the first two elements of each list
      assertKeysAreEqual(fakeKeys.get(0), firstBatch.getKeys(0));
    }

    @Test
    void shouldNotPackageKeysIfConsentFlagIsNotSet() {
      var dataset = generateRandomUploadKeys(true, minKeyThreshold);
      dataset.add(generateRandomUploadKey(false));
      var result = diagnosisKeyBatchAssembler.assembleDiagnosisKeyBatch(dataset);
      Assertions.assertEquals(1, result.size());
      Assertions.assertEquals(minKeyThreshold, result.keySet().iterator().next().getKeysCount());
    }

    @ParameterizedTest
    @ArgumentsSource(KeysToPartitionProvider.class)
    void shouldGenerateCorrectNumberOfBatches(List<FederationUploadKey> dataset, Integer expectedBatches) {
      var result = diagnosisKeyBatchAssembler.assembleDiagnosisKeyBatch(dataset);
      Assertions.assertEquals(expectedBatches, result.size());
    }
  }

  @Nested
  @EnableConfigurationProperties(value = UploadServiceConfig.class)
  @ExtendWith(SpringExtension.class)
  @ContextConfiguration(classes = {
      DiagnosisKeyBatchAssembler.class}, initializers = ConfigFileApplicationContextInitializer.class)
  class AllPropertiesDisabled {

    @MockBean
    AllowedPropertiesMap allowedPropertiesMapMock;

    @Autowired
    DiagnosisKeyBatchAssembler diagnosisKeyBatchAssembler;

    @Test
    void shouldNotSendDsosOrReportTypeIfNotAllowed() {
      when(allowedPropertiesMapMock.getDsosOrDefault(anyInt())).thenReturn(1);
      when(allowedPropertiesMapMock.getReportTypeOrDefault(any())).thenReturn(ReportType.UNKNOWN);
      var keys = generateRandomUploadKeys(true, 10);
      var result = diagnosisKeyBatchAssembler.assembleDiagnosisKeyBatch(keys);
      result.forEach((batch, diagnosisKeys) -> diagnosisKeys
          .forEach(k -> {
            Assertions.assertEquals(0, k.getDaysSinceOnsetOfSymptoms(), "DSOS should be 0");
            Assertions.assertEquals(ReportType.UNKNOWN,
                k.getReportType(), "Report Type should be UNKNOWN");
          }));
    }
  }

}
