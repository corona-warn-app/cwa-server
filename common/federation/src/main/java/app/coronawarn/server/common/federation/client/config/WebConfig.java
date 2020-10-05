

package app.coronawarn.server.common.federation.client.config;

import app.coronawarn.server.common.federation.client.download.FederationGatewayHttpMessageConverter;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableFeignClients("app.coronawarn.server.common.federation.client")
public class WebConfig {

  @Bean
  public HttpMessageConverters httpMessageConverters() {
    return new HttpMessageConverters(new FederationGatewayHttpMessageConverter());
  }
}
