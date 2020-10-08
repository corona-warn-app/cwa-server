package app.coronawarn.server.common.persistence.domain.config;

import java.io.IOException;
import java.util.Properties;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

/**
 * Because loading yaml files with @PropertySources is not supported in Spring,
 * we need this custom implementation for processing the yamls and converting them
 * to injectable properties in the Spring application context.
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

  @Override
  public PropertySource<?> createPropertySource(String name, EncodedResource encodedResource)
      throws IOException {
    YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
    factory.setResources(encodedResource.getResource());

    Properties properties = factory.getObject();

    return new PropertiesPropertySource(encodedResource.getResource().getFilename(), properties);
  }
}
