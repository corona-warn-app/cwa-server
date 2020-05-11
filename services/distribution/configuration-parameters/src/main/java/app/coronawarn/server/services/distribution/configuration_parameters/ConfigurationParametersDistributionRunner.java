package app.coronawarn.server.services.distribution.configuration_parameters;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConfigurationParametersDistributionRunner implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(ConfigurationParametersDistributionRunner.class, args);
  }

  @Override
  public void run(String... args) {
  }
}
