package app.coronawarn.server.services.eventregistration;

import java.util.Arrays;
import java.util.List;
import app.coronawarn.server.services.eventregistration.config.EventRegistrationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;

@SpringBootApplication
@EnableConfigurationProperties({EventRegistrationConfiguration.class})
@EnableJdbcRepositories(basePackages = {"app.coronawarn.server.common.persistence"})
@EntityScan(basePackages = "app.coronawarn.server.common.persistence")
@ComponentScan({"app.coronawarn.server.common.persistence"})
public class EventRegistrationApplication implements EnvironmentAware {
  static final String DISABLE_SSL_CLIENT_POSTGRES = "disable-ssl-client-postgres";
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
    List<String> profiles = Arrays.asList(environment.getActiveProfiles());

    logger.info("Enabled named groups: {}", System.getProperty("jdk.tls.namedGroups"));
    if (profiles.contains(DISABLE_SSL_CLIENT_POSTGRES)) {
      logger.warn(
          "The submission service is started with postgres connection TLS disabled. "
              + "This should never be used in PRODUCTION!");
    }
  }
}
