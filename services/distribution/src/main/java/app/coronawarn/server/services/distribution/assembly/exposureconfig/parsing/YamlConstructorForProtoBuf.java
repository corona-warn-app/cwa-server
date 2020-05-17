package app.coronawarn.server.services.distribution.assembly.exposureconfig.parsing;

import java.util.Arrays;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

/**
 * This Constructor implementation grants SnakeYaml compliance with the generated proto Java
 * classes.
 * SnakeYaml expects the Java properties to have the same name as the yaml properties. But the
 * generated Java classes' properties have an added suffix of '_'.
 * In addition, this implementation also allows snake case in the YAML (for better readability), as
 * the Java properties are transformed to camel case.
 */
public class YamlConstructorForProtoBuf extends Constructor {

  public YamlConstructorForProtoBuf() {
    setPropertyUtils(new ProtoBufPropertyUtils());
  }

  private static class ProtoBufPropertyUtils extends PropertyUtils {

    public Property getProperty(Class<?> type, String name, BeanAccess beanAccess) {
      return super.getProperty(type, transformToProtoNaming(name), beanAccess);
    }

    private String transformToProtoNaming(String yamlPropertyName) {
      return snakeToCamelCase(yamlPropertyName) + "_";
    }

    private String snakeToCamelCase(String snakeCase) {
      String camelCase = Arrays.stream(snakeCase.split("_"))
          .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
          .reduce("", String::concat);

      return Character.toLowerCase(camelCase.charAt(0)) + camelCase.substring(1);
    }
  }

}
