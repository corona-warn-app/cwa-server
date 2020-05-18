package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.distribution.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

  @Autowired
  private DiagnosisKeyService diagnosisKeyService;

  @Autowired
  private ApplicationContext applicationContext;

  @Value("${services.distribution.retention-days}")
  private Integer retentionDays;

  @Override
  public void run(ApplicationArguments args) {
    try {
      diagnosisKeyService.applyRetentionPolicy(retentionDays);
    } catch (Exception e) {
      logger.error("Application of retention policy failed.", e);
      Application.killApplication(applicationContext);
    }

    logger.debug("Retention policy applied successfully. Deleted all entries older that {} days.",
        retentionDays);
  }
}
