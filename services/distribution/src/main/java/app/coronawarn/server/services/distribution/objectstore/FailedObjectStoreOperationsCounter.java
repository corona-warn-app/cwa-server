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

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreOperationFailedException;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Maintains the count and maximum number of failed object store operations in a thread-safe manner.
 */
@Component
public class FailedObjectStoreOperationsCounter {

  private static final Logger logger = LoggerFactory.getLogger(FailedObjectStoreOperationsCounter.class);

  private final int maxNumberOfFailedOperations;
  private final AtomicInteger failedOperationsCounter = new AtomicInteger(0);

  public FailedObjectStoreOperationsCounter(DistributionServiceConfig distributionServiceConfig) {
    maxNumberOfFailedOperations = distributionServiceConfig.getObjectStore().getMaxNumberOfFailedOperations();
  }

  /**
   * Increments the internal failed operations counter and rethrows the specified exception if the configured maximum
   * number of failed object store operation was exceeded.
   *
   * @param cause The {@link ObjectStoreOperationFailedException} that is associated with the failed operation.
   */
  public void incrementAndCheckThreshold(ObjectStoreOperationFailedException cause) {
    logger.error("Object store operation failed.", cause);
    if (failedOperationsCounter.incrementAndGet() > maxNumberOfFailedOperations) {
      logger.error("Number of failed object store operations exceeded threshold of {}.", maxNumberOfFailedOperations);
      throw cause;
    }
  }
}
