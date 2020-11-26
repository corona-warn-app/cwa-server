package app.coronawarn.server.services.distribution.statistics.file;

import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!local-json-stats")
public class RemoteStatisticJsonFileLoader implements JsonFileLoader {

  @Autowired
  @Qualifier("stats-s3")
  ObjectStoreClient s3Stats;

  /**
   * Connects to remote storage to load file.
   *
   * @return String content of file
   */
  public String getContent() {
    // @TODO: load variables from config
    return s3Stats.getSingleObjectContent("obs-cwa-public-dev",
        "json/v1/cwa_reporting_public_data.json");
  }

}
