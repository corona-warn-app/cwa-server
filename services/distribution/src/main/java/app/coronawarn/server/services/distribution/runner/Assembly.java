

package app.coronawarn.server.services.distribution.runner;

import static app.coronawarn.server.services.distribution.logging.LogMessages.PREPARING_FILES;
import static app.coronawarn.server.services.distribution.logging.LogMessages.START_SIGNING;
import static app.coronawarn.server.services.distribution.logging.LogMessages.WRITING_FILES;

import app.coronawarn.server.services.distribution.assembly.component.CwaApiStructureProvider;
import app.coronawarn.server.services.distribution.assembly.component.OutputDirectoryProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.io.IOException;
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

  /**
   * Creates an Assembly, using {@link OutputDirectoryProvider}, {@link CwaApiStructureProvider} and
   * {@link ApplicationContext}.
   */
  Assembly(OutputDirectoryProvider outputDirectoryProvider,
      CwaApiStructureProvider cwaApiStructureProvider) {
    this.outputDirectoryProvider = outputDirectoryProvider;
    this.cwaApiStructureProvider = cwaApiStructureProvider;
  }

  @Override
  public void run(ApplicationArguments args) throws IOException {
    Directory<WritableOnDisk> outputDirectory = this.outputDirectoryProvider.getDirectory();
    outputDirectory.addWritable(cwaApiStructureProvider.getDirectory());
    this.outputDirectoryProvider.clear();
    logger.debug(PREPARING_FILES);
    logger.info(START_SIGNING);
    outputDirectory.prepare(new ImmutableStack<>());
    logger.debug(WRITING_FILES);
    outputDirectory.write();
  }
}
