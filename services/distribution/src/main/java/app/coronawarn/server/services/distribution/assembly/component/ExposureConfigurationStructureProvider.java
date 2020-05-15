package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.common.protocols.internal.RiskScoreParameters;
import app.coronawarn.server.services.distribution.assembly.exposureconfig.ExposureConfigurationProvider;
import app.coronawarn.server.services.distribution.assembly.exposureconfig.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.exposureconfig.structure.directory.ExposureConfigurationDirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Reads the exposure configuration parameters from the respective file in the class path and builds
 * a {@link ExposureConfigurationDirectoryImpl} with them.
 */
@Component
public class ExposureConfigurationStructureProvider {

  private static final Logger logger = LoggerFactory
      .getLogger(ExposureConfigurationStructureProvider.class);

  @Autowired
  private CryptoProvider cryptoProvider;

  public Directory getExposureConfiguration() {
    var riskScoreParameters = readExposureConfiguration();
    return new ExposureConfigurationDirectoryImpl(riskScoreParameters, cryptoProvider);
  }

  private RiskScoreParameters readExposureConfiguration() {
    logger.debug("Reading exposure configuration...");
    try {
      return ExposureConfigurationProvider.readMasterFile();
    } catch (UnableToLoadFileException e) {
      logger.error("Could not load exposure configuration parameters", e);
      throw new RuntimeException(e);
    }
  }
}
