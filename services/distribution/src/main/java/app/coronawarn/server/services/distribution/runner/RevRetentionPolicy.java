package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.common.persistence.service.DccRevocationListService;
import app.coronawarn.server.services.distribution.Application;
import app.coronawarn.server.services.distribution.dcc.DccRevocationClient;
import app.coronawarn.server.services.distribution.dcc.FetchDccListException;
import app.coronawarn.server.services.distribution.objectstore.S3RetentionPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This runner removes any diagnosis keys from the database that were submitted before a configured threshold of days.
 */
@Component
@Order(1)
@Profile("revocation")
public class RevRetentionPolicy implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(RevRetentionPolicy.class);

  private final ApplicationContext applicationContext;

  private final S3RetentionPolicy s3RetentionPolicy;

  private final DccRevocationListService dccRevocationListService;

  private final DccRevocationClient dccRevocationClient;

  /**
   * Creates a new RetentionPolicy.
   *
   * @param applicationContext       ApplicationContext
   * @param s3RetentionPolicy        S3RetentionPolicy
   * @param dccRevocationListService DccRevocationListService
   * @param dccRevocationClient      DccRevocationClient
   */
  public RevRetentionPolicy(
      final ApplicationContext applicationContext,
      final S3RetentionPolicy s3RetentionPolicy,
      final DccRevocationListService dccRevocationListService,
      final DccRevocationClient dccRevocationClient) {
    this.applicationContext = applicationContext;
    this.s3RetentionPolicy = s3RetentionPolicy;
    this.dccRevocationListService = dccRevocationListService;
    this.dccRevocationClient = dccRevocationClient;
  }

  @Override
  public void run(final ApplicationArguments args) {
    try {
      if (dccRevocationListService.etagExists(dccRevocationClient.getETag())) {
        logger.info("DCC Revocation - ETag didn't change, nothing to do, shutting down.");
        SpringApplication.exit(applicationContext);
        System.exit(0);
        return;
      }
      s3RetentionPolicy.deleteDccRevocationDir();
      logger.debug("Retention policy applied successfully.");
    } catch (final FetchDccListException e) {
      logger.warn("Couldn't fetch ETag for DCC-Revocation, will continue with assembly...", e);
    } catch (final Exception e) {
      logger.error("Application of retention policy failed.", e);
      Application.killApplication(applicationContext);
    }
  }
}
