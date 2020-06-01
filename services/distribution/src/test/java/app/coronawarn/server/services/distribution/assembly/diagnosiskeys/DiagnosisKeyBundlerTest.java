package app.coronawarn.server.services.distribution.assembly.diagnosiskeys;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class},
    initializers = ConfigFileApplicationContextInitializer.class)
class DiagnosisKeyBundlerTest {

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Nested
  @DisplayName("Expiry policy")
  class DiagnosisKeyBundlerExpiryPolicyTest {
    @ParameterizedTest
    @ValueSource(longs = {0L, 24L, 24L + 2L})
    void testLastPeriodOfHourAndSubmissionLessThanDistributionDateTime(long submissionTimestamp) {
      List<DiagnosisKey> diagnosisKeys = buildDiagnosisKeys(5, submissionTimestamp, 150);
      DiagnosisKeyBundler bundler = new DiagnosisKeyBundler(diagnosisKeys, distributionServiceConfig);
      assertThat(bundler.getDiagnosisKeysDistributableAt(LocalDateTime.of(1970, 1, 2, 3, 0, 0))).hasSize(150);
    }

    @Test
    void testLastPeriodOfHourAndSubmissionEqualsDistributionDateTime() {
      List<DiagnosisKey> diagnosisKeys = buildDiagnosisKeys(5, 24L + 3L, 150);
      DiagnosisKeyBundler bundler = new DiagnosisKeyBundler(diagnosisKeys, distributionServiceConfig);
      assertThat(bundler.getDiagnosisKeysDistributableAt(LocalDateTime.of(1970, 1, 2, 3, 0, 0))).hasSize(150);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, 24L, 24L + 2L, 24L + 3L})
    void testFirstPeriodOfHourAndSubmissionLessThanDistributionDateTime(long submissionTimestamp) {
      List<DiagnosisKey> diagnosisKeys = buildDiagnosisKeys(6, submissionTimestamp, 150);
      DiagnosisKeyBundler bundler = new DiagnosisKeyBundler(diagnosisKeys, distributionServiceConfig);
      assertThat(bundler.getDiagnosisKeysDistributableAt(LocalDateTime.of(1970, 1, 2, 4, 0, 0))).hasSize(150);
    }

    @Test
    void testFirstPeriodOfHourAndSubmissionEqualsDistributionDateTime() {
      List<DiagnosisKey> diagnosisKeys = buildDiagnosisKeys(6, 24L + 4L, 150);
      DiagnosisKeyBundler bundler = new DiagnosisKeyBundler(diagnosisKeys, distributionServiceConfig);
      assertThat(bundler.getDiagnosisKeysDistributableAt(LocalDateTime.of(1970, 1, 2, 4, 0, 0))).hasSize(150);
    }

    @Test
    void testLastPeriodOfHourAndSubmissionGreaterDistributionDateTime() {
      List<DiagnosisKey> diagnosisKeys = buildDiagnosisKeys(5, 24L + 4L, 150);
      DiagnosisKeyBundler bundler = new DiagnosisKeyBundler(diagnosisKeys, distributionServiceConfig);
      assertThat(bundler.getDiagnosisKeysDistributableAt(LocalDateTime.of(1970, 1, 2, 4, 0, 0))).hasSize(150);
    }
  }

  @Nested
  @DisplayName("Shifting policy")
  class DiagnosisKeyBundlerShiftingPolicyTest {

    @Test
    void testDoesNotShiftIfPackageSizeGreaterThanThreshold() {
      List<DiagnosisKey> diagnosisKeys = buildDiagnosisKeys(6, 50L, 141);
      DiagnosisKeyBundler bundler = new DiagnosisKeyBundler(diagnosisKeys, distributionServiceConfig);
      assertThat(bundler.getDiagnosisKeysDistributableAt(LocalDateTime.of(1970, 1, 3, 2, 0, 0))).hasSize(141);
    }

    @Test
    void testDoesNotShiftIfPackageSizeEqualsThreshold() {
      List<DiagnosisKey> diagnosisKeys = buildDiagnosisKeys(6, 50L, 140);
      DiagnosisKeyBundler bundler = new DiagnosisKeyBundler(diagnosisKeys, distributionServiceConfig);
      assertThat(bundler.getDiagnosisKeysDistributableAt(LocalDateTime.of(1970, 1, 3, 2, 0, 0))).hasSize(140);
    }

    @Test
    void testShiftsIfPackageSizeLessThanThreshold() {
      List<DiagnosisKey> diagnosisKeys = buildDiagnosisKeys(6, 50L, 139);
      DiagnosisKeyBundler bundler = new DiagnosisKeyBundler(diagnosisKeys, distributionServiceConfig);
      assertThat(bundler.getDiagnosisKeysDistributableAt(LocalDateTime.of(1970, 1, 3, 2, 0, 0))).hasSize(0);
    }
  }

  public List<DiagnosisKey> buildDiagnosisKeys(int startIntervalNumber, long submissionTimestamp, int number) {
    return IntStream.range(0, number)
        .mapToObj(__ -> DiagnosisKey.builder()
            .withKeyData(new byte[16])
            .withRollingStartIntervalNumber(startIntervalNumber)
            .withTransmissionRiskLevel(2)
            .withSubmissionTimestamp(submissionTimestamp).build())
        .collect(Collectors.toList());
  }

}
