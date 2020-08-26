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

package app.coronawarn.server.services.federation.upload;

import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
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
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

/**
 * Service responsible for creating batches of diagnosis keys and uploading them to the Federation Gateway conforming to
 * the EU specification. Its source of data is a dedicated table where diagnosis keys are replicated during the
 * submission process.
 */
@SpringBootApplication
@EnableJdbcRepositories(basePackages = "app.coronawarn.server.common.persistence")
@EntityScan(basePackages = "app.coronawarn.server.common.persistence")
@ComponentScan({"app.coronawarn.server.common.persistence",
    "app.coronawarn.server.services.federation.upload",
    "app.coronawarn.server.common.federation.client"})
@EnableConfigurationProperties({UploadServiceConfig.class})
public class Application implements EnvironmentAware, DisposableBean {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    SpringApplication.run(Application.class);
  }

  @Override
  public void destroy() {
    logger.info("Shutting down log4j2.");
    LogManager.shutdown();
  }

  @Override
  public void setEnvironment(Environment environment) {
    List<String> profiles = Arrays.asList(environment.getActiveProfiles());
    logger.info("Enabled named groups: {}", System.getProperty("jdk.tls.namedGroups"));
    if (profiles.contains("disable-ssl-client-postgres")) {
      logger.warn(
          "The upload service is started with postgres connection TLS disabled. "
              + "This should never be used in PRODUCTION!");
    }
  }
}
