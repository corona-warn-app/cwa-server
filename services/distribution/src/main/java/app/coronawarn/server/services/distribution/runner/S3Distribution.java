

package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.services.distribution.assembly.component.OutputDirectoryProvider;
import app.coronawarn.server.services.distribution.objectstore.S3Publisher;
import java.io.IOException;
import java.nio.file.Path;
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

  private final OutputDirectoryProvider outputDirectoryProvider;
  private final S3Publisher s3Publisher;

  S3Distribution(OutputDirectoryProvider outputDirectoryProvider, S3Publisher s3Publisher) {
    this.outputDirectoryProvider = outputDirectoryProvider;
    this.s3Publisher = s3Publisher;
  }

  @Override
  public void run(ApplicationArguments args) throws IOException {
    Path pathToDistribute = outputDirectoryProvider.getFileOnDisk().toPath().toAbsolutePath();
    s3Publisher.publish(pathToDistribute);
  }
}
