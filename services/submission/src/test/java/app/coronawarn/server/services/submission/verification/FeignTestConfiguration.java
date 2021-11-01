

package app.coronawarn.server.services.submission.verification;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("feign")
public class FeignTestConfiguration {
  @Bean
  public HttpMessageConverters httpMessageConverters() {
    return new HttpMessageConverters();
  }
}
