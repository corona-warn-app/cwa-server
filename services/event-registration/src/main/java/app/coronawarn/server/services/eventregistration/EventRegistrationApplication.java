package app.coronawarn.server.services.eventregistration;

import app.coronawarn.server.services.eventregistration.config.EventRegistrationConfiguration;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;

@SpringBootApplication
@EnableConfigurationProperties({EventRegistrationConfiguration.class})
@EntityScan(basePackages = "app.coronawarn.server.services.eventregistration.domain")
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@ComponentScan({"app.coronawarn.server.services.eventregistration"})
public class EventRegistrationApplication implements EnvironmentAware {

  private static final Logger logger = LoggerFactory.getLogger(EventRegistrationApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(EventRegistrationApplication.class);
  }

  @Bean
  ProtobufHttpMessageConverter protobufHttpMessageConverter() {
    return new ProtobufHttpMessageConverter();
  }

  @Override
  public void setEnvironment(Environment environment) {
    logger.info("Enabled named groups: {}", System.getProperty("jdk.tls.namedGroups"));
  }
}
