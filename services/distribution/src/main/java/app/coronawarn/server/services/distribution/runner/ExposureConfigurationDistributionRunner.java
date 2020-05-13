package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.common.protocols.internal.RiskScoreParameters;
import app.coronawarn.server.services.distribution.crypto.CryptoProvider;
import app.coronawarn.server.services.distribution.exposureconfig.ExposureConfigurationProvider;
import app.coronawarn.server.services.distribution.exposureconfig.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.exposureconfig.structure.ExposureConfigurationDirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.Directory;
import app.coronawarn.server.services.distribution.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.decorator.IndexingDecorator;
import java.io.File;
import java.util.List;
import java.util.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
/**
 * Reads the exposure configuration parameters from the respective file in the class path, then
 * generates and persists the respective exposure configuration bundle.
 */
public class ExposureConfigurationDistributionRunner implements ApplicationRunner {

  private static final Logger logger =
      LoggerFactory.getLogger(ExposureConfigurationDistributionRunner.class);

  @Value("${app.coronawarn.server.services.distribution.version}")
  private String country;

  @Value("${app.coronawarn.server.services.distribution.region}")
  private String version;

  @Value("${app.coronawarn.server.services.distribution.paths.output}")
  private String outputPath;

  @Autowired
  private CryptoProvider cryptoProvider;

  private static final String VERSION_DIRECTORY = "version";

  @Override
  public void run(ApplicationArguments args) {
    var riskScoreParameters = readExposureConfiguration();
    IndexDirectory<?> versionDirectory =
        new IndexDirectoryImpl<>(VERSION_DIRECTORY, __ -> List.of(version), Object::toString);
    ExposureConfigurationDirectoryImpl parametersDirectory =
        new ExposureConfigurationDirectoryImpl(country, riskScoreParameters, cryptoProvider);
    Directory root = new DirectoryImpl(new File(outputPath));
    versionDirectory.addDirectoryToAll(__ -> parametersDirectory);
    root.addDirectory(new IndexingDecorator<>(versionDirectory));
    root.prepare(new Stack<>());
    root.write();
  }

  private RiskScoreParameters readExposureConfiguration() {
    try {
      return ExposureConfigurationProvider.readMasterFile();
    } catch (UnableToLoadFileException e) {
      logger.error("Could not load exposure configuration parameters", e);
      throw new RuntimeException(e);
    }
  }
}
