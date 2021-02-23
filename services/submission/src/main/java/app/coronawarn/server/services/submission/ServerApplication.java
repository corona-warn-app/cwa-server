

package app.coronawarn.server.services.submission;

import app.coronawarn.server.services.submission.config.SubmissionServiceConfigValidator;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.validation.Validator;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableJdbcRepositories(basePackages = "app.coronawarn.server.common.persistence")
@EntityScan(basePackages = "app.coronawarn.server.common.persistence")
@ComponentScan({"app.coronawarn.server.common.persistence",
    "app.coronawarn.server.services.submission", "app.coronawarn.server.common.federation.client.hostname"})
@EnableConfigurationProperties
@EnableFeignClients
public class ServerApplication implements EnvironmentAware, DisposableBean {

  static final String DISABLE_SSL_CLIENT_POSTGRES = "disable-ssl-client-postgres";

  private static final Logger logger = LoggerFactory.getLogger(ServerApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(ServerApplication.class);
  }

  @Bean
  TimedAspect timedAspect(MeterRegistry registry) {
    return new TimedAspect(registry);
  }

  /**
   * Manual shutdown hook needed to avoid Log4j shutdown issues (see cwa-server/#589).
   */
  @Override
  public void destroy() {
    LogManager.shutdown();
  }

  @Bean
  ProtobufHttpMessageConverter protobufHttpMessageConverter() {
    return new ProtobufHttpMessageConverter();
  }

  @Bean
  public static Validator configurationPropertiesValidator() {
    return new SubmissionServiceConfigValidator();
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
