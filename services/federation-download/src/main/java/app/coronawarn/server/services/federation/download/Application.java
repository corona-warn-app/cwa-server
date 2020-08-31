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

package app.coronawarn.server.services.federation.download;

import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

/**
 * The retrieval, assembly and distribution of configuration and diagnosis key data is handled by a chain of {@link
 * org.springframework.boot.ApplicationRunner} instances. An unrecoverable failure in either one of them is terminating
 * the chain execution.
 */
@SpringBootApplication
@EnableJdbcRepositories(basePackages = "app.coronawarn.server.common.persistence")
@EntityScan(basePackages = "app.coronawarn.server.common.persistence")
@ComponentScan({ "app.coronawarn.server.common.persistence", "app.coronawarn.server.services.federation.download",
    "app.coronawarn.server.common.federation.client" })
@EnableConfigurationProperties
public class Application implements EnvironmentAware, DisposableBean {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    SpringApplication.run(Application.class);
  }

  /**
   * Manual shutdown hook needed to avoid Log4j shutdown issues (see cwa-server/#589).
   */
  @Override
  public void destroy() {
    logger.info("Shutting down log4j2.");
    LogManager.shutdown();
  }

  /**
   * Terminates this application with exit code 1 (general error).
   */
  public static void killApplication(ApplicationContext appContext) {
    SpringApplication.exit(appContext);
    logger.error("Application terminated abnormally.");
    System.exit(1);
  }

  @Override
  public void setEnvironment(Environment environment) {
    List<String> profiles = Arrays.asList(environment.getActiveProfiles());
    logger.info("Enabled named groups: {}", System.getProperty("jdk.tls.namedGroups"));
    if (profiles.contains("disable-ssl-server")) {
      logger.warn(
          "The submission service is started with endpoint TLS disabled. This should never be used in PRODUCTION!");
    }
    if (profiles.contains("disable-ssl-client-postgres")) {
      logger.warn(
          "The submission service is started with postgres connection TLS disabled. "
              + "This should never be used in PRODUCTION!");
    }
    if (profiles.contains("disable-ssl-client-verification")) {
      logger.warn(
          "The submission service is started with verification service connection TLS disabled. "
              + "This should never be used in PRODUCTION!");
    }
    if (profiles.contains("disable-ssl-client-verification-verify-hostname")) {
      logger.warn(
          "The submission service is started with verification service TLS hostname validation disabled. "
              + "This should never be used in PRODUCTION!");
    }
  }
}
