package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.common.persistence.service.DccRevocationListService;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.StatisticsDownloadService;
import app.coronawarn.server.common.persistence.service.TraceTimeIntervalWarningService;
import app.coronawarn.server.services.distribution.Application;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dcc.DccRevocationClient;
import app.coronawarn.server.services.distribution.objectstore.S3RetentionPolicy;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * This runner removes any diagnosis keys from the database that were submitted before a configured threshold of days.
 */
@Component
@Order(1)
public class RetentionPolicy implements ApplicationRunner {

  private static final Logger logger = LoggerFactory
      .getLogger(RetentionPolicy.class);

  private final DiagnosisKeyService diagnosisKeyService;

  private final TraceTimeIntervalWarningService traceTimeIntervalWarningService;

  private final ApplicationContext applicationContext;

  private final Integer retentionDays;

  private final S3RetentionPolicy s3RetentionPolicy;

  private final Integer hourFileRetentionDays;

  private final StatisticsDownloadService statisticsDownloadService;

  private DccRevocationListService dccRevocationListService;

  private final Environment environment;

  private DccRevocationClient dccRevocationClient;

  /**
   * Creates a new RetentionPolicy.
   *
   * @param diagnosisKeyService             DiagnosisKeyService
   * @param traceTimeIntervalWarningService TraceTimeIntervalWarningService
   * @param applicationContext              ApplicationContext
   * @param distributionServiceConfig       retention days
   * @param s3RetentionPolicy               S3RetentionPolicy
   */
  public RetentionPolicy(
      DiagnosisKeyService diagnosisKeyService,
      TraceTimeIntervalWarningService traceTimeIntervalWarningService,
      ApplicationContext applicationContext,
      DistributionServiceConfig distributionServiceConfig,
      S3RetentionPolicy s3RetentionPolicy,
      DccRevocationListService dccRevocationListService,
      final Environment environment,
      final DccRevocationClient dccRevocationClient,
      StatisticsDownloadService statisticsDownloadService) {
    this.diagnosisKeyService = diagnosisKeyService;
    this.traceTimeIntervalWarningService = traceTimeIntervalWarningService;
    this.applicationContext = applicationContext;
    this.retentionDays = distributionServiceConfig.getRetentionDays();
    this.hourFileRetentionDays = distributionServiceConfig.getObjectStore().getHourFileRetentionDays();
    this.s3RetentionPolicy = s3RetentionPolicy;
    this.dccRevocationListService = dccRevocationListService;
    this.environment = environment;
    this.dccRevocationClient = dccRevocationClient;
    this.statisticsDownloadService = statisticsDownloadService;
  }

  @Override
  public void run(ApplicationArguments args) {
    try {
      if (isDccRevocation()) {
        if (dccRevocationListService.etagExists(dccRevocationClient.getETag())) {
          logger.info("DCC Revocation - ETag didn't change, nothing to do, shutting down.");
          SpringApplication.exit(applicationContext);
          return;
        }
        dccRevocationListService.truncate();
        s3RetentionPolicy.deleteDccRevocationDir();
      } else {
        diagnosisKeyService.applyRetentionPolicy(retentionDays);
        traceTimeIntervalWarningService.applyRetentionPolicy(retentionDays);
        s3RetentionPolicy.applyDiagnosisKeyDayRetentionPolicy(retentionDays);
        s3RetentionPolicy.applyDiagnosisKeyHourRetentionPolicy(hourFileRetentionDays);
        s3RetentionPolicy.applyTraceTimeWarningHourRetentionPolicy(retentionDays);
        statisticsDownloadService.applyRetentionPolicy(retentionDays);
      }
      logger.debug("Retention policy applied successfully.");
    } catch (Exception e) {
      logger.error("Application of retention policy failed.", e);
      Application.killApplication(applicationContext);
    }
  }

  public boolean isDccRevocation() {
    return Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> env.equalsIgnoreCase("revocation"));
  }
}
