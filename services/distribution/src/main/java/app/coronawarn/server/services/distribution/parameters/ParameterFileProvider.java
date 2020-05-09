package app.coronawarn.server.services.distribution.parameters;

import java.io.InputStream;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class ParameterFileProvider {


  public Yaml readFile(String path) {
    Yaml yaml = new Yaml();
    InputStream inputStream = this.getClass()
        .getClassLoader()
        .getResourceAsStream("parameters/example.yaml");
    Map<String, Object> obj = yaml.load(inputStream);
    System.out.println(obj);
    return null;
  }

}
