

package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.services.distribution.Application;
import app.coronawarn.server.services.distribution.assembly.component.OutputDirectoryProvider;
import app.coronawarn.server.services.distribution.objectstore.S3Publisher;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreOperationFailedException;
import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This runner will sync the base working directory to the S3.
 */
@Component
@Order(3)
public class S3Distribution implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(S3Distribution.class);

  private final OutputDirectoryProvider outputDirectoryProvider;
  private final S3Publisher s3Publisher;
  private final ApplicationContext applicationContext;

  S3Distribution(OutputDirectoryProvider outputDirectoryProvider, S3Publisher s3Publisher,
      ApplicationContext applicationContext) {
    this.outputDirectoryProvider = outputDirectoryProvider;
    this.s3Publisher = s3Publisher;
    this.applicationContext = applicationContext;
  }

  @Override
  public void run(ApplicationArguments args) {
    try {
      Path pathToDistribute = outputDirectoryProvider.getFileOnDisk().toPath().toAbsolutePath();

      s3Publisher.publish(pathToDistribute);
      logger.info("Data pushed to Object Store successfully.");
    } catch (UnsupportedOperationException | ObjectStoreOperationFailedException | IOException e) {
      logger.error("Distribution failed.", e);
      Application.killApplication(applicationContext);
    }
  }
}
