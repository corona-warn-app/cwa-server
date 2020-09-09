/*
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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.services.download.DownloadServiceConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DownloadServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RetentionPolicy.class}, initializers = ConfigFileApplicationContextInitializer.class)
class RetentionPolicyTest {

  @MockBean
  FederationBatchInfoService federationBatchInfoService;

  @Autowired
  DownloadServiceConfig downloadServiceConfig;

  @Autowired
  RetentionPolicy retentionPolicy;

  @Test
  void shouldCallService() {
    retentionPolicy.run(null);

    verify(federationBatchInfoService, times(1)).applyRetentionPolicy(downloadServiceConfig.getRetentionDays());
  }
}
