package app.coronawarn.server.common.persistence.config;

import javax.sql.DataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Specifies the data source (postgres or h2) as defined
 * in the .properties files with parameters prefixed
 * by spring.datasource
 */
@Configuration
public class DataSourceConfig {

  @Bean
  @Primary
  @ConfigurationProperties(prefix = "spring.datasource")
  public DataSource dataSource() {
    return DataSourceBuilder.create().build();
  }
}
