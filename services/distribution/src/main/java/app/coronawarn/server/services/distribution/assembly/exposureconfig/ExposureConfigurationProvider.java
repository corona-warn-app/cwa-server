package app.coronawarn.server.services.distribution.assembly.exposureconfig;

import app.coronawarn.server.common.protocols.internal.RiskScoreParameters;
import app.coronawarn.server.common.protocols.internal.RiskScoreParameters.Builder;
import app.coronawarn.server.services.distribution.assembly.exposureconfig.parsing.YamlConstructorForProtoBuf;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.BeanAccess;

/**
 * Provides the Exposure Configuration based on a file in the fileystem.<br> The existing file must
 * be a valid YAML file, and must match the specification of the proto file
 * risk_score_parameters.proto.
 */
public class ExposureConfigurationProvider {

  private ExposureConfigurationProvider() {
  }

  /**
   * The location of the exposure configuration master file.
   */
  public static final String MASTER_FILE = "exposure-config/master.yaml";

  /**
   * Fetches the master configuration as a RiskScoreParameters instance.
   *
   * @return the exposure configuration as RiskScoreParameters
   * @throws UnableToLoadFileException when the file/transformation did not succeed
   */
  public static RiskScoreParameters readMasterFile() throws UnableToLoadFileException {
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
  public static RiskScoreParameters readFile(String path) throws UnableToLoadFileException {
    Yaml yaml = new Yaml(new YamlConstructorForProtoBuf());
    yaml.setBeanAccess(BeanAccess.FIELD); /* no setters on RiskScoreParameters available */

    Resource riskScoreParametersResource = new ClassPathResource(path);
    try (InputStream inputStream = riskScoreParametersResource.getInputStream()) {
      Builder loaded = yaml.loadAs(inputStream, RiskScoreParameters.newBuilder().getClass());
      if (loaded == null) {
        throw new UnableToLoadFileException(path);
      }

      return loaded.build();
    } catch (YAMLException e) {
      throw new UnableToLoadFileException("Parsing failed", e);
    } catch (IOException e) {
      throw new UnableToLoadFileException("Failed to load file " + path, e);
    }
  }
}
