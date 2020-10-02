

package app.coronawarn.server.services.distribution.runner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.S3RetentionPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RetentionPolicy.class}, initializers = ConfigFileApplicationContextInitializer.class)
class RetentionPolicyRunnerTest {

  @MockBean
  DiagnosisKeyService diagnosisKeyService;

  @MockBean
  S3RetentionPolicy s3RetentionPolicy;

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  RetentionPolicy retentionPolicy;

  @Test
  void shouldCallDatabaseAndS3RetentionRunner() {
    retentionPolicy.run(null);

    verify(diagnosisKeyService, times(1)).applyRetentionPolicy(distributionServiceConfig.getRetentionDays());
    verify(s3RetentionPolicy, times(1)).applyRetentionPolicy(distributionServiceConfig.getRetentionDays());
  }
}
