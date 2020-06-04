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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreOperationFailedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    FailedObjectStoreOperationsCounter.class}, initializers = ConfigFileApplicationContextInitializer.class)
@EnableConfigurationProperties(value = DistributionServiceConfig.class)
class FailedObjectStoreOperationsCounterTest {

  @Autowired
  private DistributionServiceConfig distributionServiceConfig;

  @Autowired
  private FailedObjectStoreOperationsCounter failedObjectStoreOperationsCounter;

  @Test
  void shouldThrowOnSixthAttempt() {
    var exception = new ObjectStoreOperationFailedException("mock");
    for (int i = 0; i < distributionServiceConfig.getObjectStore().getMaxNumberOfFailedOperations(); i++) {
      assertThatCode(() -> failedObjectStoreOperationsCounter.incrementAndCheckThreshold(exception))
          .doesNotThrowAnyException();
    }
    assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> failedObjectStoreOperationsCounter.incrementAndCheckThreshold(exception));
  }
}
