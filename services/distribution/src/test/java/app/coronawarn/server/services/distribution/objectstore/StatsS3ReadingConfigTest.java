package app.coronawarn.server.services.distribution.objectstore;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.client.S3ClientWrapper;
import app.coronawarn.server.services.distribution.objectstore.client.StatsS3ReadingConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class})
@SpringBootTest(classes = {StatsS3ReadingConfig.class})
@EnableConfigurationProperties(value = DistributionServiceConfig.class)
public class StatsS3ReadingConfigTest {

  @Autowired
  private DistributionServiceConfig distributionServiceConfig;

  @Test
  void testS3ClientWrapperInstantiation() {
    StatsS3ReadingConfig config = new StatsS3ReadingConfig();
    assertThat(config.createObjectStoreClient(distributionServiceConfig)).isInstanceOf(S3ClientWrapper.class);
  }
}
