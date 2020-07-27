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

package app.coronawarn.server.services.submission;

import app.coronawarn.server.services.submission.config.SubmissionServiceConfigValidator;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.validation.Validator;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableJdbcRepositories(basePackages = "app.coronawarn.server.common.persistence")
@EntityScan(basePackages = "app.coronawarn.server.common.persistence")
@ComponentScan({"app.coronawarn.server.common.persistence",
    "app.coronawarn.server.services.submission"})
@EnableConfigurationProperties
@EnableFeignClients
public class ServerApplication implements EnvironmentAware, DisposableBean {

  private static final Logger logger = LoggerFactory.getLogger(ServerApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(ServerApplication.class);
  }

  @Bean
  TimedAspect timedAspect(MeterRegistry registry) {
    return new TimedAspect(registry);
  }

  /**
   * Manual shutdown hook needed to avoid Log4j shutdown issues (see cwa-server/#589).
   */
  @Override
  public void destroy() {
    LogManager.shutdown();
  }

  @Bean
  ProtobufHttpMessageConverter protobufHttpMessageConverter() {
    return new ProtobufHttpMessageConverter();
  }

  @Bean
  public static Validator configurationPropertiesValidator() {
    return new SubmissionServiceConfigValidator();
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
