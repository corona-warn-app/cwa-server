package app.coronawarn.server.services.download.runner;

import app.coronawarn.server.services.download.FatalFederationGatewayException;
import app.coronawarn.server.services.download.FederationBatchProcessor;
import app.coronawarn.server.services.download.ShutdownService;
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
  private final ShutdownService shutdownService;
  private final ApplicationContext applicationContext;


  Download(FederationBatchProcessor batchProcessor, ShutdownService shutdownService,
      ApplicationContext applicationContext) {
    this.batchProcessor = batchProcessor;
    this.shutdownService = shutdownService;
    this.applicationContext = applicationContext;
  }

  @Override
  public void run(ApplicationArguments args) {
    try {
      batchProcessor.prepareDownload();
      batchProcessor.processErrorFederationBatches();
      batchProcessor.processUnprocessedFederationBatches();
    } catch (FatalFederationGatewayException e) {
      logger.error(e.getMessage());
      shutdownService.shutdownApplication(applicationContext);
    }
  }
}
