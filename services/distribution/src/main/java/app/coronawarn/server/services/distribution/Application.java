package app.coronawarn.server.services.distribution;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfigValidator;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@ComponentScan({ "app.coronawarn.server.common.persistence", "app.coronawarn.server.services.distribution",
    "app.coronawarn.server.common.federation.client.hostname" })
@EnableConfigurationProperties({ DistributionServiceConfig.class })
public class Application implements EnvironmentAware {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  @Autowired
  private static Environment env;

  public static void main(String[] args) {
    SpringApplication.run(Application.class);
  }

  @Bean
  public static Validator configurationPropertiesValidator() {
    return new DistributionServiceConfigValidator();
  }

  public static boolean isActive(final String profile) {
    return Arrays.stream(env.getActiveProfiles()).anyMatch(env -> env.equalsIgnoreCase(profile));
  }

  public static boolean isDccRevocation() {
    return isActive("revocation");
  }

  /**
   * Terminates this application with exit code 1 (general error).
   * 
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
    if (env == null) {
      env = environment;
    }
  }
}
