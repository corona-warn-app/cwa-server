/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.distribution.util;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.ObjectStore;
import app.coronawarn.server.services.distribution.objectstore.ObjectStoreAccess;
import app.coronawarn.server.services.distribution.objectstore.S3Publisher;
import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfiguration {

  ObjectStore objectStore;

  public AsyncConfiguration(DistributionServiceConfig distributionServiceConfig) {
    this.objectStore = distributionServiceConfig.getObjectStore();
  }

  /**
   * Creates an Executor, which is used by {@link S3Publisher} to multi-thread the S3 put operation. Requests to {@link
   * ObjectStoreAccess} putObject method will be automatically proxied by SpringBoot and thus run multithreaded.
   * Daemonized threads need to be used here, in order to allow Java to "naturally" terminate once all threads are
   * finished.
   *
   * @return the executor, which tells SpringBoot the basic parameters.
   */
  @Bean()
  public Executor s3TaskExecutor() {
    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(this.objectStore.getMaxNumberOfS3Threads());
    executor.setMaxPoolSize(this.objectStore.getMaxNumberOfS3Threads());
    executor.setThreadFactory(new S3UploadDaemonThreadFactory());
    executor.initialize();
    return executor;
  }
}
