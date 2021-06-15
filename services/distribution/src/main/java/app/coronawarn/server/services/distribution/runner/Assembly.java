package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.Application;
import app.coronawarn.server.services.distribution.assembly.component.CwaApiStructureProvider;
import app.coronawarn.server.services.distribution.assembly.component.OutputDirectoryProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This runner assembles and writes diagnosis key bundles and the parameter configuration.
 */
@Component
@Order(2)
public class Assembly implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(Assembly.class);

  private final OutputDirectoryProvider outputDirectoryProvider;

  private final CwaApiStructureProvider cwaApiStructureProvider;

  private final ApplicationContext applicationContext;

  /**
   * Creates an Assembly, using {@link OutputDirectoryProvider}, {@link CwaApiStructureProvider} and
   * {@link ApplicationContext}.
   */
  Assembly(OutputDirectoryProvider outputDirectoryProvider,
      CwaApiStructureProvider cwaApiStructureProvider, ApplicationContext applicationContext) {
    this.outputDirectoryProvider = outputDirectoryProvider;
    this.cwaApiStructureProvider = cwaApiStructureProvider;
    this.applicationContext = applicationContext;
  }

  @Override
  public void run(ApplicationArguments args) {
    try {
      Directory<WritableOnDisk> outputDirectory = this.outputDirectoryProvider.getDirectory();
      outputDirectory.addWritable(cwaApiStructureProvider.getDirectory());
      outputDirectory.addWritable(cwaApiStructureProvider.getDirectoryV2());
      this.outputDirectoryProvider.clear();
      logger.debug("Preparing files...");
      logger.info("Start signing...");
      outputDirectory.prepare(new ImmutableStack<>());
      logger.debug("Writing files...");
      outputDirectory.write();
      logger.debug("Distribution data assembled successfully.");
    } catch (Exception e) {
      logger.error("Distribution data assembly failed.", e);
      Application.killApplication(applicationContext);
    }
  }
}
