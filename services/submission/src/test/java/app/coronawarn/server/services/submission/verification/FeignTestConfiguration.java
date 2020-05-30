package app.coronawarn.server.services.submission.verification;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients
@Configuration
public class FeignTestConfiguration {
  @Bean
  public HttpMessageConverters httpMessageConverters() {
    return new HttpMessageConverters();
  }
}
