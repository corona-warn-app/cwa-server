package app.coronawarn.server.services.distribution.assembly.appconfig.parsing;

import java.util.Arrays;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.Tag;

public class DashToCamelCaseConstructor extends Constructor {

  public DashToCamelCaseConstructor(String path) {
    setPropertyUtils(new CamelCasePropertyUtils());
    this.yamlConstructors.put(new Tag("!include"), new IncludeConstruct(path));
  }

  private static class CamelCasePropertyUtils extends PropertyUtils {

    @Override
    public Property getProperty(Class<?> type, String name, BeanAccess beanAccess) {
      return super.getProperty(type, transformToProtoNaming(name), beanAccess);
    }

    private String transformToProtoNaming(String yamlPropertyName) {
      return snakeToCamelCase(yamlPropertyName);
    }

    private String snakeToCamelCase(String snakeCase) {
      String camelCase = Arrays.stream(snakeCase.replace("-", "_").split("_"))
          .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
          .reduce("", String::concat);

      return Character.toLowerCase(camelCase.charAt(0)) + camelCase.substring(1);
    }
  }
}