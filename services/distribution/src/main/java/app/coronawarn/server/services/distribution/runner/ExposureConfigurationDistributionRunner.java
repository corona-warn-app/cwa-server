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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class ExposureConfigurationDistributionRunner implements ApplicationRunner {

  private static final String COUNTRY = "DE";
  private static final String VERSION = "v1";
  private static final String OUTPUT_PATH = "out";

  @Autowired
  private CryptoProvider cryptoProvider;

  @Override
  public void run(ApplicationArguments args) {
    var riskScoreParameters = readRiskScoreParameters();
    IndexDirectory<?> versionDirectory =
        new IndexDirectoryImpl<>("version", __ -> List.of(VERSION));
    ExposureConfigurationDirectoryImpl parametersDirectory =
        new ExposureConfigurationDirectoryImpl(COUNTRY, riskScoreParameters, cryptoProvider);
    Directory root = new DirectoryImpl(new File(OUTPUT_PATH));
    versionDirectory.addDirectoryToAll(__ -> parametersDirectory);
    root.addDirectory(new IndexingDecorator(versionDirectory));
    root.prepare(new Stack<>());
    root.write();
  }

  private RiskScoreParameters readRiskScoreParameters() {
    try {
      return ExposureConfigurationProvider.readMasterFile();
    } catch (UnableToLoadFileException e) {
      // TODO log appropriately
      throw new RuntimeException(e);
    }
  }
}
