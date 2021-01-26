package app.coronawarn.server.services.distribution.statistics.file;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreClient;
import app.coronawarn.server.services.distribution.statistics.exceptions.BucketNotFoundException;
import app.coronawarn.server.services.distribution.statistics.exceptions.ConnectionException;
import app.coronawarn.server.services.distribution.statistics.exceptions.FilePathNotFoundException;
import app.coronawarn.server.services.distribution.statistics.exceptions.NotModifiedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.ExhaustedRetryException;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import java.util.Optional;

@Component
@Profile("!local-json-stats")
public class RemoteStatisticJsonFileLoader implements JsonFileLoader {

  private static final Logger logger = LoggerFactory.getLogger(RemoteStatisticJsonFileLoader.class);
  ObjectStoreClient s3Stats;
  DistributionServiceConfig config;


  RemoteStatisticJsonFileLoader(@Qualifier("stats-s3") ObjectStoreClient s3Stats, DistributionServiceConfig config) {
    this.s3Stats = s3Stats;
    this.config = config;
  }

  /**
   * Map parent retryable exception {@link ExhaustedRetryException} to cwa owned exceptions.
   * The inner exception will be an S3 AwsException.
   *
   * @param ex {@link software.amazon.awssdk.core.exception.SdkException} wrapped in a {@link ExhaustedRetryException}.
   * @return cwa owned RuntimeException.
   */
  private RuntimeException mapException(ExhaustedRetryException ex) {
    if (ex.getCause() instanceof NoSuchBucketException) {
      return new BucketNotFoundException(config.getStatistics().getBucket());
    } else if (ex.getCause() instanceof S3Exception) {
      return new FilePathNotFoundException(config.getStatistics().getStatisticPath());
    } else {
      return new ConnectionException();
    }
  }

  /**
   * Connects to remote storage to load file.
   *
   * @return String content of file.
   * @throws RuntimeException if errors found using AWS SDK.
   */
  @Override
  public JsonFile getFile() {
    try {
      return s3Stats.getSingleObjectContent(config.getStatistics().getBucket(),
          config.getStatistics().getStatisticPath());
    } catch (ExhaustedRetryException ex) {
      throw mapException(ex);
    }
  }

  /**
   * Connects to remote storage to load file if not modified compared to {@param eTag}.
   *
   * @param eTag only loads file if remote eTag is different from {@param eTag}.
   * @return String content of file.
   * @throws RuntimeException if errors found using AWS SDK.
   */
  @Override
  public Optional<JsonFile> getFileIfUpdated(String eTag) {
    try {
      var result = s3Stats.getSingleObjectContent(config.getStatistics().getBucket(),
          config.getStatistics().getStatisticPath(), eTag);
      return Optional.of(result);
    } catch (ExhaustedRetryException | NotModifiedException ex) {
      if (ex.getCause() instanceof NotModifiedException || ex instanceof NotModifiedException) {
        return Optional.empty();
      } else {
        throw mapException((ExhaustedRetryException) ex);
      }
    }
  }

}
