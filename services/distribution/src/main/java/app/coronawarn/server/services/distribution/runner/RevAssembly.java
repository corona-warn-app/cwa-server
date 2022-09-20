package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.Application;
import app.coronawarn.server.services.distribution.assembly.component.DccRevocationListStructureProvider;
import app.coronawarn.server.services.distribution.assembly.component.OutputDirectoryProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This runner assembles and writes diagnosis key bundles and the parameter configuration.
 */
@Component
@Order(2)
@Profile("revocation")
public class RevAssembly implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(RevAssembly.class);

  private final OutputDirectoryProvider outputDirectoryProvider;

  private final ApplicationContext applicationContext;

  private final DccRevocationListStructureProvider dccRevocationListStructureProvider;

  /**
   * Creates a RevAssembly, using {@link OutputDirectoryProvider}, {@link DccRevocationListStructureProvider}
   * {@link ApplicationContext}.
   */
  RevAssembly(final OutputDirectoryProvider outputDirectoryProvider, final ApplicationContext applicationContext,
      final DccRevocationListStructureProvider dccRevocationListStructureProvider) {
    this.outputDirectoryProvider = outputDirectoryProvider;
    this.applicationContext = applicationContext;
    this.dccRevocationListStructureProvider = dccRevocationListStructureProvider;
  }

  @Override
  public void run(final ApplicationArguments args) {
    try {
      final Directory<WritableOnDisk> outputDirectory = outputDirectoryProvider.getDirectory();
      dccRevocationListStructureProvider.fetchDccRevocationList();
      outputDirectoryProvider.clear();
      outputDirectory.addWritable(dccRevocationListStructureProvider.getDccRevocationDirectory());
      logger.debug("Preparing files...");
      logger.info("Start signing...");
      outputDirectory.prepare(new ImmutableStack<>());
      logger.debug("Writing files...");
      outputDirectory.write();
      logger.info("DCC Revocation data assembled successfully.");
    } catch (final Exception e) {
      logger.error("Data assembly failed.", e);
      Application.killApplication(applicationContext);
    }
  }
}
