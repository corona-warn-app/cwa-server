

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
