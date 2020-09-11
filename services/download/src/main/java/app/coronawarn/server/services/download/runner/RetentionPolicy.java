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

package app.coronawarn.server.services.download.runner;

import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.services.download.DownloadServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This runner removes any batch information from the database that were submitted before a configured threshold of
 * days.
 */
@Component
@Order(1)
public class RetentionPolicy implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(RetentionPolicy.class);

  private final FederationBatchInfoService federationBatchInfoService;
  private final Integer retentionDays;

  /**
   * Creates a new RetentionPolicy.
   */
  public RetentionPolicy(FederationBatchInfoService federationBatchInfoService,
      DownloadServiceConfig downloadServiceConfig) {
    this.federationBatchInfoService = federationBatchInfoService;
    this.retentionDays = downloadServiceConfig.getRetentionDays();
  }

  @Override
  public void run(ApplicationArguments args) {
    try {
      federationBatchInfoService.applyRetentionPolicy(retentionDays);
    } catch (Exception e) {
      logger.error("Application of retention policy failed.", e);
    }
    logger.debug("Retention policy applied successfully.");
  }
}
