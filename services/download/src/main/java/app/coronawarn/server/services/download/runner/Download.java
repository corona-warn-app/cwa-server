

package app.coronawarn.server.services.download.runner;

import app.coronawarn.server.services.download.DownloadServiceConfig;
import app.coronawarn.server.services.download.FederationBatchProcessor;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
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
  private final DownloadServiceConfig serviceConfig;

  Download(FederationBatchProcessor batchProcessor, DownloadServiceConfig serviceConfig) {
    this.batchProcessor = batchProcessor;
    this.serviceConfig = serviceConfig;
  }

  @Override
  public void run(ApplicationArguments args) {
    LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minus(Period.ofDays(serviceConfig.getEfgsOffsetDays()));
    batchProcessor.saveFirstBatchInfoForDate(yesterday);
    batchProcessor.processErrorFederationBatches();
    batchProcessor.processUnprocessedFederationBatches();
  }
}
