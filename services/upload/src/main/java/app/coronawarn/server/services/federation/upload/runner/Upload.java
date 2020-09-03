package app.coronawarn.server.services.federation.upload.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class Upload implements ApplicationRunner {

  private static final Logger logger = LoggerFactory
      .getLogger(Upload.class);

  @Override
  public void run(ApplicationArguments args) throws Exception {
    logger.info("Running Upload Job");
  }
}
