package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.services.distribution.assembly.component.OutputDirectoryProvider;
import app.coronawarn.server.services.distribution.objectstore.ObjectStoreAccess;
import app.coronawarn.server.services.distribution.objectstore.S3Publisher;
import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This runner will sync the base working directory to the S3.
 */
@Component
@Order(3)
public class S3Distribution implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(S3Distribution.class);

  @Autowired
  private OutputDirectoryProvider outputDirectoryProvider;

  @Autowired
  private ObjectStoreAccess objectStoreAccess;

  @Override
  public void run(ApplicationArguments args) {
    try {

      Path pathToDistribute = outputDirectoryProvider.getFileOnDisk().toPath().toAbsolutePath();
      S3Publisher s3Publisher = new S3Publisher(pathToDistribute, objectStoreAccess);

      s3Publisher.publish();
    } catch (UnsupportedOperationException | IOException e) {
      logger.error("Distribution failed.", e);
    }
    logger.debug("Data pushed to CDN successfully.");
  }
}
