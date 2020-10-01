

package app.coronawarn.server.services.submission.config;

import app.coronawarn.server.services.submission.controller.SubmissionController;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private static final String ACTUATOR_ROUTE = "/actuator/";
  private static final String HEALTH_ROUTE = ACTUATOR_ROUTE + "health";
  private static final String PROMETHEUS_ROUTE = ACTUATOR_ROUTE + "prometheus";
  private static final String READINESS_ROUTE = ACTUATOR_ROUTE + "readiness";
  private static final String LIVENESS_ROUTE = ACTUATOR_ROUTE + "liveness";
  private static final String SUBMISSION_ROUTE =
      "/version/v1" + SubmissionController.SUBMISSION_ROUTE;

  @Bean
  protected HttpFirewall strictFirewall() {
    StrictHttpFirewall firewall = new StrictHttpFirewall();
    firewall.setAllowedHttpMethods(Arrays.asList(
        HttpMethod.GET.name(),
        HttpMethod.POST.name()));
    return firewall;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .mvcMatchers(HttpMethod.GET, HEALTH_ROUTE, PROMETHEUS_ROUTE, READINESS_ROUTE, LIVENESS_ROUTE).permitAll()
        .mvcMatchers(HttpMethod.POST, SUBMISSION_ROUTE).permitAll()
        .anyRequest().denyAll()
        .and().csrf().disable();
    http.headers().contentSecurityPolicy("default-src 'self'");
  }

}
