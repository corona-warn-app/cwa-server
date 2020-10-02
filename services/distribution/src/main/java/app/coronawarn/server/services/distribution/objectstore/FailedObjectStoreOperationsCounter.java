

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
