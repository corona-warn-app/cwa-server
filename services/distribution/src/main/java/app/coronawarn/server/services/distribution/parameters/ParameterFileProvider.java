package app.coronawarn.server.services.distribution.parameters;

import app.coronawarn.server.common.protocols.internal.RiskScoreParameters;
import java.io.InputStream;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

public class ParameterFileProvider {

  public RiskScoreParameters readFile(String path) throws UnableToLoadFileException {
    Yaml yaml = new Yaml(new YamlConstructorForProtoBuf());
    yaml.setBeanAccess(BeanAccess.FIELD);

    InputStream inputStream = this.getClass()
        .getClassLoader()
        .getResourceAsStream(path);

    var loaded = yaml.loadAs(inputStream, RiskScoreParameters.newBuilder().getClass());
    if (loaded == null) {
      throw new UnableToLoadFileException(path);
    }

    return loaded.build();
  }

}
