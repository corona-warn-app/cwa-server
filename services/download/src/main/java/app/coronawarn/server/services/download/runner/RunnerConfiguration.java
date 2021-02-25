package app.coronawarn.server.services.download.runner;

import app.coronawarn.server.services.download.FederationBatchProcessor;
import app.coronawarn.server.services.download.ShutdownService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

@Configuration
public class RunnerConfiguration {

  @Autowired
  FederationBatchProcessor federationBatchProcessor;

  @Autowired
  ShutdownService shutdownService;

  @Autowired
  ApplicationContext applicationContext;

  @Bean
  @Order(2)
  @Profile("connect-efgs")
  Download createEfgsDownloadRunner() {
    return new Download(federationBatchProcessor, shutdownService, applicationContext);
  }

  @Bean
  @Order(2)
  @Profile("connect-chgs")
  Download createSgsDOwnloadRunner() {
    return new Download(federationBatchProcessor, shutdownService, applicationContext);
  }

}
