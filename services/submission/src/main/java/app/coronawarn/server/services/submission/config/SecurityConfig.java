

package app.coronawarn.server.services.submission.config;

import app.coronawarn.server.services.submission.controller.SubmissionController;
import java.util.Arrays;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class SecurityConfig {

  private static final String ACTUATOR_ROUTE = "/actuator";
  private static final String HEALTH_ROUTE = ACTUATOR_ROUTE + "/health";
  private static final String PROMETHEUS_ROUTE = ACTUATOR_ROUTE + "/prometheus";
  private static final String READINESS_ROUTE = HEALTH_ROUTE + "/readiness";
  private static final String LIVENESS_ROUTE = HEALTH_ROUTE + "/liveness";
  private static final String SUBMISSION_ROUTE =
      "/version/v1" + SubmissionController.SUBMISSION_ROUTE;
  private static final String SUBMISSION_ON_BEHALF_ROUTE =
      "/version/v1" + SubmissionController.SUBMISSION_ON_BEHALF_ROUTE;

  @Bean
  protected HttpFirewall strictFirewall() {
    StrictHttpFirewall firewall = new StrictHttpFirewall();
    firewall.setAllowedHttpMethods(Arrays.asList(
        HttpMethod.GET.name(),
        HttpMethod.POST.name()));
    return firewall;
  }

  /**
   * Security Filter Chain bean is configured here because it is encouraged a more component-based approach.
   * Before this we used to extend WebSecurityConfigurerAdapter (now deprecated) and Override the configure method.
   *
   * @return newly configured http bean
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .mvcMatchers(HttpMethod.GET, HEALTH_ROUTE, PROMETHEUS_ROUTE, READINESS_ROUTE, LIVENESS_ROUTE).permitAll()
        .mvcMatchers(HttpMethod.POST, SUBMISSION_ROUTE, SUBMISSION_ON_BEHALF_ROUTE).permitAll()
        .anyRequest().denyAll()
        .and().csrf().disable();
    http.headers().contentSecurityPolicy("default-src 'self'");
    return http.build();
  }

  /**
   * Validation factory bean is configured here because its message interpolation mechanism is considered a potential
   * threat if enabled.
   *
   * @return newly configured factory bean
   */
  @Bean
  public static LocalValidatorFactoryBean defaultValidator() {
    LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
    factoryBean.setMessageInterpolator(new ParameterMessageInterpolator());
    return factoryBean;
  }
}
