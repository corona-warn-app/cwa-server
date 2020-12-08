package app.coronawarn.server.services.distribution.statistics.file;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!local-json-stats")
public class RemoteStatisticJsonFileLoader implements JsonFileLoader {

  ObjectStoreClient s3Stats;

  DistributionServiceConfig config;


  RemoteStatisticJsonFileLoader(@Qualifier("stats-s3") ObjectStoreClient s3Stats, DistributionServiceConfig config) {
    this.s3Stats = s3Stats;
    this.config = config;
  }

  /**
   * Connects to remote storage to load file.
   *
   * @return String content of file
   */
  public String getContent() {
    return s3Stats.getSingleObjectContent(config.getStatistics().getBucket(),
        config.getStatistics().getStatisticPath());
  }

}
