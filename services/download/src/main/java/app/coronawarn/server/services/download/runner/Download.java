

package app.coronawarn.server.services.download.runner;

import app.coronawarn.server.services.download.Application;
import app.coronawarn.server.services.download.FatalFederationGatewayException;
import app.coronawarn.server.services.download.FederationBatchProcessor;
import app.coronawarn.server.services.download.config.DownloadServiceConfig;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This runner retrieves diagnosis key batches.
 */
@Component
@Order(2)
public class Download implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(Download.class);

  private final FederationBatchProcessor batchProcessor;
  private final DownloadServiceConfig serviceConfig;
  private final ApplicationContext applicationContext;

  Download(FederationBatchProcessor batchProcessor, DownloadServiceConfig serviceConfig,
      ApplicationContext applicationContext) {
    this.batchProcessor = batchProcessor;
    this.serviceConfig = serviceConfig;
    this.applicationContext = applicationContext;
  }

  @Override
  public void run(ApplicationArguments args) {
    try {
      LocalDate downloadDate = LocalDate.now(ZoneOffset.UTC).minus(Period.ofDays(serviceConfig.getEfgsOffsetDays()));
      batchProcessor.saveFirstBatchInfoForDate(downloadDate);
      batchProcessor.processErrorFederationBatches();
      batchProcessor.processUnprocessedFederationBatches();
    } catch (FatalFederationGatewayException e) {
      logger.error(e.getMessage());
      Application.killApplication(applicationContext);
    }
  }
}
