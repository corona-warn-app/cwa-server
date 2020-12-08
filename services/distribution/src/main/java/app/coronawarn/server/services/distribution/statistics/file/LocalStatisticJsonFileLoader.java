package app.coronawarn.server.services.distribution.statistics.file;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
@Profile("local-json-stats")
public class LocalStatisticJsonFileLoader implements JsonFileLoader {

  @Autowired
  ResourceLoader resourceLoader;

  @Autowired
  DistributionServiceConfig serviceConfig;

  /**
   * Test method to get content from test file.
   *
   * @return String content of file
   */
  public String getContent() {
    var resource = resourceLoader.getResource(
        String.format("classpath:%s", serviceConfig.getStatistics().getStatisticPath()));
    try {
      return FileUtils.readFileToString(resource.getFile(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(String.format("Failed to load Local JSON from path %s",
          serviceConfig.getStatistics().getStatisticPath()));
    }
  }

}
