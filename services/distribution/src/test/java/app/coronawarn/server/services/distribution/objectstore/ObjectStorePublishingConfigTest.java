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

package app.coronawarn.server.services.distribution.objectstore;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreClient;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStorePublishingConfig;
import app.coronawarn.server.services.distribution.objectstore.client.S3ClientWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class})
@SpringBootTest(classes = {ObjectStorePublishingConfig.class})
@EnableConfigurationProperties(value = DistributionServiceConfig.class)
class ObjectStorePublishingConfigTest {

  @MockBean
  private ObjectStoreClient objectStoreClient;

  @Autowired
  private ThreadPoolTaskExecutor executor;

  @Autowired
  private DistributionServiceConfig distributionServiceConfig;

  @Test
  void testS3ClientWrapperInstantiation() {
    ObjectStorePublishingConfig config = new ObjectStorePublishingConfig();
    assertThat(config.createObjectStoreClient(distributionServiceConfig)).isInstanceOf(S3ClientWrapper.class);
  }

  @Test
  void testThreadPoolExecutorPoolSize() {
    int expNumberOfThreads = distributionServiceConfig.getObjectStore().getMaxNumberOfS3Threads();
    assertThat(executor.getCorePoolSize()).isEqualTo(expNumberOfThreads);
    assertThat(executor.getMaxPoolSize()).isEqualTo(expNumberOfThreads);
  }
}
