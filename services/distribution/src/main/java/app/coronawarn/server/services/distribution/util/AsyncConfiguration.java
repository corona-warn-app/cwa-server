package app.coronawarn.server.services.distribution.util;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfiguration {

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  private static final Logger logger = LoggerFactory.getLogger(AsyncConfiguration.class);

  public AsyncConfiguration(DistributionServiceConfig distributionServiceConfig) {
    this.distributionServiceConfig = distributionServiceConfig;
  }

  /**
   * todo.
   */
  @Bean (name = "s3TaskExecutor")
  public Executor taskExecutor() {
    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(4);
//    executor.setAllowCoreThreadTimeOut(true);
    executor.setKeepAliveSeconds(10);
    executor.setThreadNamePrefix("s3Operation-");
    executor.initialize();
    return executor;
  }

}
