package app.coronawarn.server.services.submission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;

@SpringBootApplication
@ServletComponentScan
@EnableJpaRepositories(basePackages = "app.coronawarn.server.services.common.persistence")
@EntityScan(basePackages = "app.coronawarn.server.services.common.persistence")
@ComponentScan({"app.coronawarn.server.services.common.persistence", "app.coronawarn.server.services.submission"})
public class ServerApplication {

  @Bean
  ProtobufHttpMessageConverter protobufHttpMessageConverter() {
    return new ProtobufHttpMessageConverter();
  }

  public static void main(String[] args) {
    SpringApplication.run(ServerApplication.class, args);
  }
}
