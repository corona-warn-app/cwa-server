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

package app.coronawarn.server.services.distribution;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * The retrieval, assembly and distribution of configuration and diagnosis key data is handled by a chain of {@link
 * org.springframework.boot.ApplicationRunner} instances. An unrecoverable failure in either one of them is terminating
 * the chain execution.
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "app.coronawarn.server.common.persistence")
@EntityScan(basePackages = "app.coronawarn.server.common.persistence")
@ComponentScan({"app.coronawarn.server.common.persistence",
    "app.coronawarn.server.services.distribution"})
@EnableConfigurationProperties({DistributionServiceConfig.class})
public class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    SpringApplication.run(Application.class);
  }

  /**
   * Terminates this application with exit code 1 (general error).
   */
  public static void killApplication(ApplicationContext appContext) {
    SpringApplication.exit(appContext);
    logger.error("Application terminated abnormally.");
    System.exit(1);
  }
}
