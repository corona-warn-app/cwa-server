package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This runner removes any diagnosis keys from the database that were submitted before a configured
 * threshold of days.
 */
@Component
@Order(1)
public class RetentionPolicyRunner implements ApplicationRunner {

  private static final Logger logger = LoggerFactory
      .getLogger(RetentionPolicyRunner.class);

  @Autowired
  private DiagnosisKeyService diagnosisKeyService;

  @Value("${app.coronawarn.server.services.distribution.retention_days}")
  private Integer rententionDays;

  @Override
  public void run(ApplicationArguments args) {
    diagnosisKeyService.applyRetentionPolicy(rententionDays);

    logger.debug("Retention policy applied successfully. Deleted all entries older that {} days.",
        rententionDays);
  }
}
