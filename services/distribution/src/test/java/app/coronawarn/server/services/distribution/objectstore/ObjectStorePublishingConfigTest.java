

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
