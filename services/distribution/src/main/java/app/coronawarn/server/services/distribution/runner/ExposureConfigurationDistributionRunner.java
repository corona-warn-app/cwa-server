package app.coronawarn.server.services.distribution.runner;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class ExposureConfigurationDistributionRunner implements ApplicationRunner {

  @Override
  public void run(ApplicationArguments args) {
  }
}
