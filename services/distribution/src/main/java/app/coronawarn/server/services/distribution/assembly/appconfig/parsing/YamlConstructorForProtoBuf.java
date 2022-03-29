package app.coronawarn.server.services.distribution.assembly.appconfig.parsing;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * This Constructor implementation grants SnakeYaml compliance with the generated proto Java classes. SnakeYaml expects
 * the Java properties to have the same name as the yaml properties. But the generated Java classes' properties have an
 * added suffix of '_'. In addition, this implementation also allows snake case in the YAML (for better readability), as
 * the Java properties are transformed to camel case.
 */
public class YamlConstructorForProtoBuf extends Constructor {

  private static final Logger logger = LoggerFactory.getLogger(YamlConstructorForProtoBuf.class);

  public YamlConstructorForProtoBuf(String path) {
    setPropertyUtils(new ProtoBufPropertyUtils());
    this.yamlConstructors.put(new Tag("!include"), new IncludeConstruct(path));
  }

  /**
   * If a scalar contains a variable like <code>${...}</code> it will be replaced by the setting of the environment
   * variable, in case it exists. You can also provide defaults like this:<br/><code>foo: ${BAR:42}</code>.
   */
  @Override
  protected String constructScalar(final ScalarNode node) {
    String scalar = super.constructScalar(node);
    if (scalar.startsWith("${") && '}' == scalar.charAt(scalar.length() - 1)) {
      logger.debug("environment variable substitution: {}", scalar);
      String env = scalar.substring(2, scalar.length() - 2);
      final int index = env.indexOf(':');
      String defaultValue = "";
      if (index > -1) {
        env = env.substring(0, index);
        defaultValue = scalar.substring(scalar.indexOf(':') + 1, scalar.length() - 1).trim();
      }
      scalar = System.getenv(env);
      if (scalar == null) {
        logger.debug("enviroment {} not set, using default '{}'", env, defaultValue);
        if (defaultValue.isEmpty()) {
          logger.error(
              "{} is undefined and no default is set! Please check your yaml-files and your environment settings.",
              env);
        }
        return defaultValue;
      }
      logger.debug("using system setting: '{}' for {}", scalar, env);
    }
    return scalar;
  }

  private static class ProtoBufPropertyUtils extends PropertyUtils {

    @Override
    public Property getProperty(Class<?> type, String name, BeanAccess beanAccess) {
      return super.getProperty(type, transformToProtoNaming(name), beanAccess);
    }

    private String transformToProtoNaming(String yamlPropertyName) {
      return snakeToCamelCase(yamlPropertyName) + "_";
    }

    private String snakeToCamelCase(String snakeCase) {
      String camelCase = Arrays.stream(snakeCase.replace("-", "_").split("_"))
          .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
          .reduce("", String::concat);

      return Character.toLowerCase(camelCase.charAt(0)) + camelCase.substring(1);
    }
  }
}
