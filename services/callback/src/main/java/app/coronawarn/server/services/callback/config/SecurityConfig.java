package app.coronawarn.server.services.callback.config;

import static java.util.Collections.emptyList;

import app.coronawarn.server.services.callback.CertificateCnMismatchException;
import app.coronawarn.server.services.callback.controller.CallbackController;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private static final String CALLBACK_ROUTE =
      "/version/v1" + CallbackController.CALLBACK_ROUTE;
  private static final String ACTUATOR_ROUTE = "/actuator";
  private static final String HEALTH_ROUTE = ACTUATOR_ROUTE + "/health";
  private static final String PROMETHEUS_ROUTE = ACTUATOR_ROUTE + "/prometheus";
  private static final String READINESS_ROUTE = HEALTH_ROUTE + "/readiness";
  private static final String LIVENESS_ROUTE = HEALTH_ROUTE + "/liveness";
  private final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
  private final CallbackServiceConfig callbackServiceConfig;

  @Autowired
  public SecurityConfig(CallbackServiceConfig callbackServiceConfig) {
    this.callbackServiceConfig = callbackServiceConfig;
  }

  @Bean
  protected HttpFirewall strictFirewall() {
    StrictHttpFirewall firewall = new StrictHttpFirewall();
    firewall.setAllowedHttpMethods(Collections.singletonList(
        HttpMethod.GET.name()));
    return firewall;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry expressionInterceptUrlRegistry
        = http.authorizeRequests();
    expressionInterceptUrlRegistry
        .mvcMatchers(HttpMethod.GET, CALLBACK_ROUTE).authenticated().and().x509()
        .userDetailsService(userDetailsService());
    expressionInterceptUrlRegistry
        .mvcMatchers(HttpMethod.GET, HEALTH_ROUTE, PROMETHEUS_ROUTE, READINESS_ROUTE, LIVENESS_ROUTE).permitAll();
    expressionInterceptUrlRegistry
        .anyRequest().denyAll();
    http.headers().contentSecurityPolicy("default-src 'self'");
  }


  /**
   * The UserDetailsService will check if the CN of the client certificate matches the expected CN defined in the
   * application.yaml
   *
   * @return UserDetailsService
   */
  @Bean
  @Override
  public UserDetailsService userDetailsService() {
    return username -> {
      if (username.equals(callbackServiceConfig.getCertCn())) {
        return new User(username, "", emptyList());
      }
      String exceptionMsg =
          "The client certificate CN '"
              + username
              + "' does not match the expected CN: '"
              + callbackServiceConfig.getCertCn() + "'.";
      logger.warn(exceptionMsg);
      throw new CertificateCnMismatchException(exceptionMsg);
    };
  }
}
