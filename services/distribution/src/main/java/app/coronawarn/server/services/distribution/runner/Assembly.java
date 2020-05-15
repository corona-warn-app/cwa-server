package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.services.distribution.assembly.component.OutputDirectory;
import app.coronawarn.server.services.distribution.assembly.component.Version;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
/**
 * This runner assembles and writes diagnosis key bundles and the parameter configuration.
 */
public class Assembly implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(Assembly.class);

  @Autowired
  private OutputDirectory outputDirectory;

  @Autowired
  private Version version;

  @Override
  public void run(ApplicationArguments args) throws IOException {
    Directory outputDirectory = this.outputDirectory.getDirectory();
    outputDirectory.addDirectory(version.getDirectory());
    this.outputDirectory.clear();
    logger.debug("Preparing files...");
    outputDirectory.prepare(new ImmutableStack<>());
    logger.debug("Writing files...");
    outputDirectory.write();
    logger.debug("Distribution run finished successfully.");
  }
}
