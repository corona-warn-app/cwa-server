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
   * Creates an Executor, which is used by {@link app.coronawarn.server.services.distribution.objectstore.S3Publisher}
   * to multi thread the S3 put operation. Requests to
   * {@link app.coronawarn.server.services.distribution.objectstore.ObjectStoreAccess} putObject method will be
   * automatically proxied by SpringBoot and thus run multithreaded. Daemonized threads need to be used here, in order
   * to allow Java to "naturally" terminate once all threads are finished.
   *
   * @return the executor, which tells SpringBoot the basic parameters.
   */
  @Bean(name = "s3TaskExecutor")
  public Executor taskExecutor() {
    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    // TODO
    executor.setCorePoolSize(8);
    executor.setMaxPoolSize(8);
    executor.setThreadNamePrefix("s3Operation-");
    // This is working just fine as well, would introduce an AWS SDK dependency, which isn't wanted here I guess
    // TODO: discuss
    //executor.setThreadFactory(new ThreadFactoryBuilder().daemonThreads(true).build());
    executor.setThreadFactory(new DaemonThreadFactory());
    executor.initialize();
    return executor;
  }
}
