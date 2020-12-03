package app.coronawarn.server.services.distribution.objectstore.client;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.StatisticsConfig;
import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableRetry
public class StatsS3ReadingConfig {

  public static final String REGION_1 = "region-1";

  @Bean(name = "stats-s3")
  public ObjectStoreClient createObjectStoreClient(DistributionServiceConfig distributionServiceConfig) {
    return createClient(distributionServiceConfig.getStatistics());
  }

  private ObjectStoreClient createClient(StatisticsConfig s3stats) {
    AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
        AwsBasicCredentials.create(s3stats.getAccessKey(), s3stats.getSecretKey()));
    String endpoint = removeTrailingSlash(s3stats.getEndpoint());

    return new S3ClientWrapper(S3Client.builder()
        .region(Region.of(REGION_1))
        .endpointOverride(URI.create(endpoint))
        .credentialsProvider(credentialsProvider)
        .build());
  }

  private String removeTrailingSlash(String string) {
    return string.endsWith("/") ? string.substring(0, string.length() - 1) : string;
  }

}
