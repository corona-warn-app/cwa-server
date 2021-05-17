package app.coronawarn.server.services.download.runner;

import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.services.download.config.DownloadServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This runner removes any batch information from the database that were submitted before a configured threshold of
 * days.
 */
@Component
@Order(1)
public class RetentionPolicy implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(RetentionPolicy.class);

  private final FederationBatchInfoService federationBatchInfoService;
  private final Integer retentionDays;

  /**
   * Creates a new RetentionPolicy.
   *
   * @param federationBatchInfoService batch info service {@link FederationBatchInfoService}
   * @param downloadServiceConfig download service configuration {@link DownloadServiceConfig}
   */
  public RetentionPolicy(FederationBatchInfoService federationBatchInfoService,
      DownloadServiceConfig downloadServiceConfig) {
    this.federationBatchInfoService = federationBatchInfoService;
    this.retentionDays = downloadServiceConfig.getRetentionDays();
  }

  @Override
  public void run(ApplicationArguments args) {
    try {
      federationBatchInfoService.applyRetentionPolicy(retentionDays);
      logger.debug("Retention policy applied successfully.");
    } catch (Exception e) {
      logger.error("Application of retention policy failed.", e);
    }
  }
}
