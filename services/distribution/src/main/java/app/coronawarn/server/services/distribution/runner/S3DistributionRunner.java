package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.services.distribution.objectstore.S3Publisher;
import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This runner will sync the base working directory to the S3.
 */
@Component
@Order(3)
public class S3DistributionRunner implements ApplicationRunner {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @Value("${app.coronawarn.server.services.distribution.paths.output}")
  private String workdir;

  @Autowired
  private S3Publisher s3Publisher;

  @Override
  public void run(ApplicationArguments args) {
    try {
      Path pathToDistribute = Path.of(workdir).toAbsolutePath();

      s3Publisher.publishFolder(pathToDistribute);
    } catch (IOException | UnsupportedOperationException e) {
      logger.error("Distribution failed.", e);
    }
    logger.debug("Data pushed to CDN successfully.");
  }
}
