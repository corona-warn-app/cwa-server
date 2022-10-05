package app.coronawarn.server.services.distribution.runner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.service.DccRevocationListService;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.StatisticsDownloadService;
import app.coronawarn.server.common.persistence.service.TraceTimeIntervalWarningService;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dcc.DccRevocationClient;
import app.coronawarn.server.services.distribution.dcc.FetchDccListException;
import app.coronawarn.server.services.distribution.objectstore.S3RetentionPolicy;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RevRetentionPolicy.class }, initializers = ConfigDataApplicationContextInitializer.class)
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
  RevRetentionPolicy retentionPolicy;

  @MockBean
  DccRevocationListService dccRevocationListService;

  @MockBean
  DccRevocationClient dccRevocationClient;

  @MockBean
  ApplicationContext applicationContext;

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

  @Test
  void shouldCheckForEtag() throws FetchDccListException {
    when(dccRevocationListService.etagExists(dccRevocationClient.getETag())).thenReturn(true);
    Assertions.assertThat(SpringApplication.exit(applicationContext));
    retentionPolicy.run(null);
    verify(s3RetentionPolicy, times(1)).deleteDccRevocationDir();
  }

  @Test
  void shouldThrowExceptionForEtag() throws FetchDccListException {
    when(dccRevocationClient.getETag()).thenThrow(FetchDccListException.class);
    retentionPolicy.run(null);
    verify(s3RetentionPolicy, times(0)).deleteDccRevocationDir();
  }

  @Test
  void shouldThrowAnyException() throws FetchDccListException {
    when(dccRevocationClient.getETag()).thenThrow(RuntimeException.class);
    retentionPolicy.run(null);
    verify(s3RetentionPolicy, times(0)).deleteDccRevocationDir();
  }
}
