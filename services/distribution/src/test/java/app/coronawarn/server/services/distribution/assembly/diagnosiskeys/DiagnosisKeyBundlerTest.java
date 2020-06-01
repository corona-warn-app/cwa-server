package app.coronawarn.server.services.distribution.assembly.diagnosiskeys;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.List;
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
public class DiagnosisKeyBundlerTest {

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  /*
  private DiagnosisKeyBundler diagnosisKeyBundler;
  @BeforeEach
  public void setup() {
    diagnosisKeyBundler = new DiagnosisKeyBundler(List.of(), distributionServiceConfig);
  }
  */

  @ParameterizedTest
  @ValueSource(longs = {0L, 24L, 24L + 2L})
  void testLastPeriodOfHourAndSubmissionLessThanDistributionDateTime(long submissionTimestamp) {
    DiagnosisKey diagnosisKey = buildDiagnosisKey(5, submissionTimestamp);
    DiagnosisKeyBundler diagnosisKeyBundler = new DiagnosisKeyBundler(List.of(diagnosisKey), distributionServiceConfig);
    assertThat(diagnosisKeyBundler.getDistributionDateTime(diagnosisKey)).isEqualTo("1970-01-02T03:00");
  }

  @Test
  void testLastPeriodOfHourAndSubmissionEqualsDistributionDateTime() {
    DiagnosisKey diagnosisKey = buildDiagnosisKey(5, 24L + 3L);
    DiagnosisKeyBundler diagnosisKeyBundler = new DiagnosisKeyBundler(List.of(diagnosisKey), distributionServiceConfig);
    assertThat(diagnosisKeyBundler.getDistributionDateTime(diagnosisKey)).isEqualTo("1970-01-02T03:00");
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, 24L, 24L + 2L, 24L + 3L})
  void testFirstPeriodOfHourAndSubmissionLessThanDistributionDateTime(long submissionTimestamp) {
    DiagnosisKey diagnosisKey = buildDiagnosisKey(6, submissionTimestamp);
    DiagnosisKeyBundler diagnosisKeyBundler = new DiagnosisKeyBundler(List.of(diagnosisKey), distributionServiceConfig);
    assertThat(diagnosisKeyBundler.getDistributionDateTime(diagnosisKey)).isEqualTo("1970-01-02T04:00");
  }

  @Test
  void testFirstPeriodOfHourAndSubmissionEqualsDistributionDateTime() {
    DiagnosisKey diagnosisKey = buildDiagnosisKey(6, 24L + 4L);
    DiagnosisKeyBundler diagnosisKeyBundler = new DiagnosisKeyBundler(List.of(diagnosisKey), distributionServiceConfig);
    assertThat(diagnosisKeyBundler.getDistributionDateTime(diagnosisKey)).isEqualTo("1970-01-02T04:00");
  }

  @Test
  void testLastPeriodOfHourAndSubmissionGreaterDistributionDateTime() {
    DiagnosisKey diagnosisKey = buildDiagnosisKey(5, 24L + 4L);
    DiagnosisKeyBundler diagnosisKeyBundler = new DiagnosisKeyBundler(List.of(diagnosisKey), distributionServiceConfig);
    assertThat(diagnosisKeyBundler.getDistributionDateTime(diagnosisKey)).isEqualTo("1970-01-02T04:00");
  }

  private DiagnosisKey buildDiagnosisKey(int startIntervalNumber, long submissionTimestamp) {
    return DiagnosisKey.builder()
        .withKeyData(new byte[16])
        .withRollingStartIntervalNumber(startIntervalNumber)
        .withTransmissionRiskLevel(2)
        .withSubmissionTimestamp(submissionTimestamp).build();
  }
}
