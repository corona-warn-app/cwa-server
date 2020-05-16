package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.services.distribution.assembly.component.CwaApiStructureProvider;
import app.coronawarn.server.services.distribution.assembly.component.OutputDirectoryProvider;
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

/**
 * This runner assembles and writes diagnosis key bundles and the parameter configuration.
 */
@Component
@Order(2)
public class Assembly implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(Assembly.class);

  @Autowired
  private OutputDirectoryProvider outputDirectoryProvider;

  @Autowired
  private CwaApiStructureProvider cwaApiStructureProvider;

  @Override
  public void run(ApplicationArguments args) throws IOException {
    Directory outputDirectory = this.outputDirectoryProvider.getDirectory();
    outputDirectory.addDirectory(cwaApiStructureProvider.getDirectory());
    this.outputDirectoryProvider.clear();
    logger.debug("Preparing files...");
    outputDirectory.prepare(new ImmutableStack<>());
    logger.debug("Writing files...");
    outputDirectory.write();
    logger.debug("Distribution run finished successfully.");
  }
}
