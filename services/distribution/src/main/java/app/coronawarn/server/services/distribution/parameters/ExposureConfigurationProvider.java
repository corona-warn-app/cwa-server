package app.coronawarn.server.services.distribution.parameters;

import app.coronawarn.server.common.protocols.internal.RiskScoreParameters;
import app.coronawarn.server.services.distribution.parameters.parsing.YamlConstructorForProtoBuf;
import java.io.InputStream;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.BeanAccess;

/**
 * Provides the Exposure Configuration based on a file in the fileystem.<br> The existing file must
 * be a valid YAML file, and must match the specification of the proto file
 * risk_score_parameters.proto.
 */
public class ExposureConfigurationProvider {

  /**
   * the location of the exposure configuration master file
   */
  public static final String MASTER_FILE = "exposure-config/master.yaml";

  /**
   * Fetches the master configuration as a RiskScoreParameters instance.
   *
   * @return the exposure configuration as RiskScoreParameters
   * @throws UnableToLoadFileException when the file/transformation did not succeed
   */
  public RiskScoreParameters readMasterFile() throws UnableToLoadFileException {
    return readFile(MASTER_FILE);
  }

  /**
   * Fetches an exposure configuration file based on the given path. The path must be available in
   * the classloader.
   *
   * @param path the path, e.g. folder/my-exposure-configuration.yaml
   * @return the RiskScoreParameters
   * @throws UnableToLoadFileException when the file/transformation did not succeed
   */
  public RiskScoreParameters readFile(String path) throws UnableToLoadFileException {
    Yaml yaml = new Yaml(new YamlConstructorForProtoBuf());
    yaml.setBeanAccess(BeanAccess.FIELD); /* no setters on RiskScoreParameters available */

    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(path);

    try {
      var loaded = yaml.loadAs(inputStream, RiskScoreParameters.newBuilder().getClass());
      if (loaded == null) {
        throw new UnableToLoadFileException(path);
      }

      return loaded.build();
    } catch (YAMLException e) {
      throw new UnableToLoadFileException("Parsing failed", e);
    }
  }
}
