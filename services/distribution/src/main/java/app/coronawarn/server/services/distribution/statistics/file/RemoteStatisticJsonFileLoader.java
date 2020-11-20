package app.coronawarn.server.services.distribution.statistics.file;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!local-json-stats")
public class RemoteStatisticJsonFileLoader {

  /**
   * Connects to remote storage to load file.
   *
   * @return String content of file
   */
  public String getContent() {
    // TODO: Implement
    return "";
  }

}
