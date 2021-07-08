

package app.coronawarn.server.services.distribution;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfigValidator;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.validation.Validator;

/**
 * The retrieval, assembly and distribution of configuration and diagnosis key data is handled by a chain of {@link
 * org.springframework.boot.ApplicationRunner} instances. An unrecoverable failure in either one of them is terminating
 * the chain execution.
 */
@SpringBootApplication
@EnableJdbcRepositories(basePackages = "app.coronawarn.server.common.persistence")
@EntityScan(basePackages = "app.coronawarn.server.common.persistence")
@ComponentScan({"app.coronawarn.server.common.persistence", "app.coronawarn.server.services.distribution",
    "app.coronawarn.server.common.federation.client.hostname"})
@EnableConfigurationProperties({DistributionServiceConfig.class})
public class Application implements EnvironmentAware, DisposableBean {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    SpringApplication.run(Application.class);
  }

  /**
   * Manual shutdown hook needed to avoid Log4j shutdown issues (see cwa-server/#589).
   */
  @Override
  public void destroy() {
    logger.info("Shutting down log4j2.");
    LogManager.shutdown();
  }

  @Bean
  public static Validator configurationPropertiesValidator() {
    return new DistributionServiceConfigValidator();
  }

  /**
   * Terminates this application with exit code 1 (general error).
   * @param appContext type ApplicationContext
   */
  public static void killApplication(ApplicationContext appContext) {
    SpringApplication.exit(appContext);
    logger.error("Application terminated abnormally.");
    System.exit(1);
  }

  @Override
  public void setEnvironment(Environment environment) {
    List<String> profiles = Arrays.asList(environment.getActiveProfiles());
    if (profiles.contains("disable-ssl-client-postgres")) {
      logger.warn("The distribution runner is started with postgres connection TLS disabled. "
          + "This should never be used in PRODUCTION!");
    }
  }
}
