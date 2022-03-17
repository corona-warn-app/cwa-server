package app.coronawarn.server.services.distribution.assembly.component;

import static org.junit.Assert.assertNotNull;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.io.Files;

class OutputDirectoryProviderTest {

  private final DistributionServiceConfig mockConf = new DistributionServiceConfig() {
    @Override
    public Paths getPaths() {
      return new Paths() {
        @Override
        public String getOutput() {
          return Files.createTempDir().getAbsolutePath();
        }
      };
    }
  };

  @Test
  void testClear() throws IOException {
    OutputDirectoryProvider p = new OutputDirectoryProvider(mockConf);
    p.clear();
  }

  @Test
  void testGetDirectory() {
    OutputDirectoryProvider p = new OutputDirectoryProvider(mockConf);
    assertNotNull(p.getDirectory());
  }
}
