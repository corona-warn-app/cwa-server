package app.coronawarn.server.services.distribution.statistics.file;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreClient;
import app.coronawarn.server.services.distribution.statistics.exceptions.BucketNotFoundException;
import app.coronawarn.server.services.distribution.statistics.exceptions.ConnectionException;
import app.coronawarn.server.services.distribution.statistics.exceptions.FilePathNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.ExhaustedRetryException;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

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
    try {
      return s3Stats.getSingleObjectContent(config.getStatistics().getBucket(),
          config.getStatistics().getStatisticPath());
    } catch (ExhaustedRetryException ex) {
      if (ex.getCause() instanceof NoSuchBucketException) {
        throw new BucketNotFoundException(config.getStatistics().getBucket());
      } else if (ex.getCause() instanceof S3Exception) {
        throw new FilePathNotFoundException(config.getStatistics().getStatisticPath());
      } else {
        throw new ConnectionException();
      }
    }
  }

}
