package app.coronawarn.server.services.distribution.statistics.file;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
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
  public JsonFile getFile() {
    var resource = resourceLoader.getResource(
        String.format("classpath:%s", serviceConfig.getStatistics().getStatisticPath()));
    try {
      String content = FileUtils.readFileToString(resource.getFile(), StandardCharsets.UTF_8);
      return new JsonFile(content, "local");
    } catch (IOException e) {
      throw new RuntimeException(String.format("Failed to load Local JSON from path %s",
          serviceConfig.getStatistics().getStatisticPath()), e);
    }
  }

  @Override
  public Optional<JsonFile> getFileIfUpdated(String etag) {
    return Optional.of(this.getFile());
  }

}
