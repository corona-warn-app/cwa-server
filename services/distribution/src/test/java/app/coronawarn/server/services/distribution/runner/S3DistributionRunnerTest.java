

package app.coronawarn.server.services.distribution.runner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.assembly.component.OutputDirectoryProvider;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.S3Publisher;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {S3Distribution.class}, initializers = ConfigFileApplicationContextInitializer.class)
class S3DistributionRunnerTest {

  @MockBean
  OutputDirectoryProvider outputDirectoryProvider;

  @MockBean
  S3Publisher s3Publisher;

  @Autowired
  S3Distribution s3Distribution;

  @Test
  void shouldPublishCorrectFolder() throws IOException {
    var outputPath = Paths.get("test", "mock", "folder");
    var outputFile = new java.io.File(String.valueOf(outputPath));

    when(outputDirectoryProvider.getFileOnDisk()).thenReturn(outputFile);

    s3Distribution.run(null);

    verify(s3Publisher, times(1)).publish(outputPath.toAbsolutePath());
  }
}
