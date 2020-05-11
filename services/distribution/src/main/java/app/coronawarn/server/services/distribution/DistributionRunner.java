package app.coronawarn.server.services.distribution;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "app.coronawarn.server.services.common.persistence")
@EntityScan(basePackages = "app.coronawarn.server.services.common.persistence")
@ComponentScan({"app.coronawarn.server.services.common.persistence",
    "app.coronawarn.server.services.distribution"})
public class DistributionRunner implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(DistributionRunner.class, args);
  }

  @Override
  public void run(String... args) {
  }
}
