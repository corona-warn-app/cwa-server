

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import app.coronawarn.server.services.distribution.common.Helpers;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.unit.DataSize;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, KeySharingPoliciesChecker.class, ProdDiagnosisKeyBundler.class},
    initializers = ConfigFileApplicationContextInitializer.class)
class ProdDiagnosisKeyBundlerExpiryPolicyTest {

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  DiagnosisKeyBundler bundler;

  @ParameterizedTest
  @ValueSource(longs = {0L, 24L, 24L + 2L})
  void testLastPeriodOfHourAndSubmissionLessThanDistributionDateTime(long submissionTimestamp) {
    List<DiagnosisKey> diagnosisKeys = Helpers.buildDiagnosisKeys(5, submissionTimestamp, 10);
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 3, 0, 0), "DE")).hasSize(10);
  }

  @Test
  void testLastPeriodOfHourAndSubmissionEqualsDistributionDateTime() {
    List<DiagnosisKey> diagnosisKeys = Helpers.buildDiagnosisKeys(5, 24L + 3L, 10);
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 3, 0, 0), "DE")).hasSize(10);
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, 24L, 24L + 2L, 24L + 3L})
  void testFirstPeriodOfHourAndSubmissionLessThanDistributionDateTime(long submissionTimestamp) {
    List<DiagnosisKey> diagnosisKeys = Helpers.buildDiagnosisKeys(6, submissionTimestamp, 10);
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 4, 0, 0), "DE")).hasSize(10);
  }

  @Test
  void testFirstPeriodOfHourAndSubmissionEqualsDistributionDateTime() {
    List<DiagnosisKey> diagnosisKeys = Helpers.buildDiagnosisKeys(6, 24L + 4L, 10);
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 4, 0, 0), "DE")).hasSize(10);
  }

  @Test
  void testLastPeriodOfHourAndSubmissionGreaterDistributionDateTime() {
    List<DiagnosisKey> diagnosisKeys = Helpers.buildDiagnosisKeys(5, 24L + 4L, 10);
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 4, 0, 0), "DE")).hasSize(10);
  }

  @Test
  void testHourlyDistributionDateTimeWithFlexibleRollingPeriod() {
    List<DiagnosisKey> diagnosisKeys = Helpers.buildDiagnosisKeysWithFlexibleRollingPeriod(144, 24 + 8L, 5, 54);
    diagnosisKeys.addAll(Helpers.buildDiagnosisKeysWithFlexibleRollingPeriod(144, 24L + 8L, 5, 84));
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 2, 13, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 12, 0, 0), "DE")).hasSize(5);
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 2, 15, 0));
    assertThat(bundler.getAllDiagnosisKeys("DE")).hasSize(5);
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 2, 18, 0));
    assertThat(bundler.getAllDiagnosisKeys("DE")).hasSize(10);
  }

  @Test
  void testDistributionDateTimeWithFlexibleRollingPeriod() {
    List<DiagnosisKey> diagnosisKeys = Helpers.buildDiagnosisKeysWithFlexibleRollingPeriod(144, 24 + 8L, 5, 54);
    diagnosisKeys.addAll(Helpers.buildDiagnosisKeysWithFlexibleRollingPeriod(144, 24L + 8L, 5, 89));
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 2, 17, 30));
    assertThat(bundler.getAllDiagnosisKeys("DE")).hasSize(5);
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 2, 18, 30));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 17, 0, 0), "DE")).hasSize(5);
  }
}
