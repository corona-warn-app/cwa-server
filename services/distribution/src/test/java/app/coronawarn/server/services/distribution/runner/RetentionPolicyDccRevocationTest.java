package app.coronawarn.server.services.distribution.runner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import app.coronawarn.server.common.persistence.service.DccRevocationListService;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.StatisticsDownloadService;
import app.coronawarn.server.common.persistence.service.TraceTimeIntervalWarningService;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dcc.DccRevocationClient;
import app.coronawarn.server.services.distribution.objectstore.S3RetentionPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RetentionPolicy.class }, initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("revocation")
class RetentionPolicyDccRevocationTest {

  @MockBean
  DiagnosisKeyService diagnosisKeyService;

  @MockBean
  TraceTimeIntervalWarningService traceTimeIntervalWarningService;

  @MockBean
  S3RetentionPolicy s3RetentionPolicy;

  @MockBean
  StatisticsDownloadService statisticsDownloadService;

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  RetentionPolicy retentionPolicy;

  @MockBean
  DccRevocationListService dccRevocationListService;

  @MockBean
  DccRevocationClient dccRevocationClient;

  @Test
  void shouldCallDatabaseAndS3RetentionRunner() {
    retentionPolicy.run(null);
    verify(statisticsDownloadService, times(0))
        .applyRetentionPolicy(distributionServiceConfig.getRetentionDays());
    verify(diagnosisKeyService, times(0))
        .applyRetentionPolicy(distributionServiceConfig.getRetentionDays());
    verify(traceTimeIntervalWarningService, times(0))
        .applyRetentionPolicy(distributionServiceConfig.getRetentionDays());

    verify(s3RetentionPolicy, times(1)).deleteDccRevocationDir();
  }
}
