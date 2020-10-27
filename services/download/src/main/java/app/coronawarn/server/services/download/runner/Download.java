package app.coronawarn.server.services.download.runner;

import app.coronawarn.server.services.download.FederationBatchProcessor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This runner retrieves diagnosis key batches.
 */
@Component
@Order(2)
public class Download implements ApplicationRunner {

  private final FederationBatchProcessor batchProcessor;

  Download(FederationBatchProcessor batchProcessor) {
    this.batchProcessor = batchProcessor;
  }

  @Override
  public void run(ApplicationArguments args) {
    batchProcessor.prepareDownload();
    batchProcessor.processErrorFederationBatches();
    batchProcessor.processUnprocessedFederationBatches();
  }
}
