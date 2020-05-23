package app.coronawarn.server.services.distribution.assembly.exposureconfig;

import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration;

public class ApplicationConfigurationProvider {

  /**
   * The location of the exposure configuration master file.
   */
  public static final String MASTER_FILE = "config/app-config.yaml";

  /**
   * Fetches the master configuration as a RiskScoreParameters instance.
   *
   * @return the exposure configuration as RiskScoreParameters
   * @throws UnableToLoadFileException when the file/transformation did not succeed
   */
  public static ApplicationConfiguration readMasterFile() throws UnableToLoadFileException {
    return readFile(MASTER_FILE);
  }

  /**
   * Fetches an exposure configuration file based on the given path. The path must be available in the classloader.
   *
   * @param path the path, e.g. folder/my-exposure-configuration.yaml
   * @return the RiskScoreParameters
   * @throws UnableToLoadFileException when the file/transformation did not succeed
   */
  public static ApplicationConfiguration readFile(String path) throws UnableToLoadFileException {
    return YamlLoader.loadYamlIntoProtobufBuilder(path, ApplicationConfiguration.Builder.class).build();
  }
}
