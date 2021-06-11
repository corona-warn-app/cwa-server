package app.coronawarn.server.services.distribution.statistics.file;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.StatisticType;
import java.io.IOException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
@Profile("local-json-stats")
public class MockStatisticJsonFileLoader implements StatisticJsonFileLoader {

  @Autowired
  ResourceLoader resourceLoader;

  @Autowired
  DistributionServiceConfig serviceConfig;

  /**
   * Test method to get content from test file.
   *
   * @return String content of file
   */
  public JsonFile getFile(StatisticType statisticType) {
    String resourcePath;

    switch (statisticType) {
      case LOCAL:
        resourcePath = serviceConfig.getStatistics().getLocalStatisticPath();
        break;
      default:
        resourcePath = serviceConfig.getStatistics().getStatisticPath();
    }

    var resource = resourceLoader.getResource(String.format("classpath:%s", resourcePath));

    try {
      return new JsonFile(resource.getInputStream(), "local");
    } catch (IOException e) {
      throw new RuntimeException(String.format("Failed to load Local JSON from path %s",
          serviceConfig.getStatistics().getStatisticPath()), e);
    }
  }

  @Override
  public Optional<JsonFile> getFileIfUpdated(StatisticType statisticType, String etag) {
    return Optional.of(this.getFile(statisticType));
  }

}
