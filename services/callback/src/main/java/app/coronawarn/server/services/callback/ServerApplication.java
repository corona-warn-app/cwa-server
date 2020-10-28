

package app.coronawarn.server.services.callback;

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
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableJdbcRepositories(basePackages = "app.coronawarn.server.common.persistence")
@EntityScan(basePackages = "app.coronawarn.server.common.persistence")
@ComponentScan({"app.coronawarn.server.common.persistence",
    "app.coronawarn.server.services.callback"})
@EnableConfigurationProperties
public class ServerApplication implements EnvironmentAware, DisposableBean {

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

  @Override
  public void setEnvironment(Environment environment) {
    List<String> profiles = Arrays.asList(environment.getActiveProfiles());

    logger.info("Enabled named groups: {}", System.getProperty("jdk.tls.namedGroups"));
    if (profiles.contains("disable-ssl-server")) {
      logger.warn(
          "The callback service is started with endpoint TLS disabled. This should never be used in PRODUCTION!");
    }
    if (profiles.contains("disable-ssl-client-postgres")) {
      logger.warn(
          "The callback service is started with postgres connection TLS disabled. "
              + "This should never be used in PRODUCTION!");
    }
    if (profiles.contains("disable-ssl-client-verification")) {
      logger.warn(
          "The callback service is started with verification service connection TLS disabled. "
              + "This should never be used in PRODUCTION!");
    }
    if (profiles.contains("disable-ssl-client-verification-verify-hostname")) {
      logger.warn(
          "The callback service is started with verification service TLS hostname validation disabled. "
              + "This should never be used in PRODUCTION!");
    }
  }
}
