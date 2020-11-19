package app.coronawarn.server.services.distribution.statistics.file;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class StatisticJsonFileLoader {

  @Autowired
  ResourceLoader resourceLoader;

  /**
   * Test method to get content from test file.
   * @return String content of file
   */
  public String getContent() {
    var resource = resourceLoader.getResource("classpath:stats/statistic_data.json");
    try {
      return FileUtils.readFileToString(resource.getFile(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
      return "";
    }
  }

}
