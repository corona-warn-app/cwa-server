

package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.distribution.Application;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.S3RetentionPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
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

  private final ApplicationContext applicationContext;

  private final Integer retentionDays;

  private final S3RetentionPolicy s3RetentionPolicy;


  /**
   * Creates a new RetentionPolicy.
   */
  public RetentionPolicy(DiagnosisKeyService diagnosisKeyService,
      ApplicationContext applicationContext,
      DistributionServiceConfig distributionServiceConfig,
      S3RetentionPolicy s3RetentionPolicy) {
    this.diagnosisKeyService = diagnosisKeyService;
    this.applicationContext = applicationContext;
    this.retentionDays = distributionServiceConfig.getRetentionDays();
    this.s3RetentionPolicy = s3RetentionPolicy;
  }

  @Override
  public void run(ApplicationArguments args) {
    try {
      diagnosisKeyService.applyRetentionPolicy(retentionDays);
      s3RetentionPolicy.applyRetentionPolicy(retentionDays);
    } catch (Exception e) {
      logger.error("Application of retention policy failed.", e);
      Application.killApplication(applicationContext);
    }

    logger.debug("Retention policy applied successfully.");
  }
}
