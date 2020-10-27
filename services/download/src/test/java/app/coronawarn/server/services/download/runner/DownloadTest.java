

package app.coronawarn.server.services.download.runner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import app.coronawarn.server.services.download.config.DownloadServiceConfig;
import app.coronawarn.server.services.download.FederationBatchProcessor;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = {FederationBatchProcessor.class})
@DirtiesContext
class DownloadTest {

  @MockBean
  private FederationBatchProcessor federationBatchProcessor;

  @Test
  void testRun() {
    Download download = new Download(federationBatchProcessor);

    download.run(null);

    verify(federationBatchProcessor, times(1)).prepareDownload();;
    verify(federationBatchProcessor, times(1)).processErrorFederationBatches();
    verify(federationBatchProcessor, times(1)).processUnprocessedFederationBatches();
  }
}
