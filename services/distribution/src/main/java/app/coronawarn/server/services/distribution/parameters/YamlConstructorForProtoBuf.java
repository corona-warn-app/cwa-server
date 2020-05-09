package app.coronawarn.server.services.distribution.parameters;

import java.util.Arrays;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

public class YamlConstructorForProtoBuf extends Constructor {

  public YamlConstructorForProtoBuf() {
    setPropertyUtils(new ProtoBufPropertyUtils());
  }

  private class ProtoBufPropertyUtils extends PropertyUtils {

    public Property getProperty(Class<? extends Object> type, String name, BeanAccess bAccess) {
      return super.getProperty(type, transformToProtoNaming(name), bAccess);
    }

    private String transformToProtoNaming(String yamlPropertyName) {
      return snakeToCamelCase(yamlPropertyName) + "_";
    }

    private String snakeToCamelCase(String snakeCase) {
      var camelCase = Arrays.stream(snakeCase.split("_"))
          .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
          .reduce("", String::concat);

      return Character.toLowerCase(camelCase.charAt(0)) + camelCase.substring(1);
    }
  }

}
