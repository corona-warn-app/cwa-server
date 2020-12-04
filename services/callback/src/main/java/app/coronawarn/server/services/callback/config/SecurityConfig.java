package app.coronawarn.server.services.callback.config;

import static java.util.Collections.emptyList;

import app.coronawarn.server.services.callback.controller.CallbackController;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Profile("!disable-callback-cert-auth")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private static final String CALLBACK_ROUTE =
      "/version/v1" + CallbackController.CALLBACK_ROUTE;

  @Bean
  protected HttpFirewall strictFirewall() {
    StrictHttpFirewall firewall = new StrictHttpFirewall();
    firewall.setAllowedHttpMethods(Arrays.asList(
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
        .anyRequest().denyAll();
    http.headers().contentSecurityPolicy("default-src 'self'");
  }

  /**
   * We do not use UserDetails, so return stub.
   *
   * @return UserDetailsService stub
   */
  @Bean
  @Override
  public UserDetailsService userDetailsService() {
    return username -> new User(username, "", emptyList());
  }
}
