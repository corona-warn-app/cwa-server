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

  private final String SUBMISSION_ROUTE = "/version/v1" + SubmissionController.SUBMISSION_ROUTE;

  @Bean
  protected HttpFirewall strictFirewall() {
    StrictHttpFirewall firewall = new StrictHttpFirewall();
    firewall.setAllowedHttpMethods(Arrays.asList(HttpMethod.POST.name()));
    return firewall;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .mvcMatchers(HttpMethod.POST, SUBMISSION_ROUTE).permitAll()
        .anyRequest().denyAll()
        .and().csrf().disable();
  }

}
