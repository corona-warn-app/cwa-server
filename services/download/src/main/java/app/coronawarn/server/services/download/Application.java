package app.coronawarn.server.services.download;

import app.coronawarn.server.services.download.config.DownloadServiceConfigValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;


@SpringBootApplication
@EnableJdbcRepositories(basePackages = "app.coronawarn.server.common.persistence")
@EntityScan(basePackages = "app.coronawarn.server.common.persistence")
@ComponentScan({"app.coronawarn.server.common.persistence", "app.coronawarn.server.services.download",
    "app.coronawarn.server.common.federation.client"})
@EnableConfigurationProperties
public class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    SpringApplication.run(Application.class);
  }

  /**
   * Terminates this application with exit code 1 (general error).
   * @param appContext type ApplicationContext
   */
  public static void killApplication(ApplicationContext appContext) {
    SpringApplication.exit(appContext);
    logger.error("Federation Download Service terminated abnormally.");
    System.exit(1);
  }

  @Bean
  public static Validator configurationPropertiesValidator() {
    return new DownloadServiceConfigValidator();
  }

  /**
   * Validation factory bean is configured here because its message interpolation mechanism
   * is considered a potential threat if enabled.
   *
   * @return new factory bean {@link LocalValidatorFactoryBean}
   */
  @Bean
  public static LocalValidatorFactoryBean defaultValidator() {
    LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
    factoryBean.setMessageInterpolator(new ParameterMessageInterpolator());
    return factoryBean;
  }
}
