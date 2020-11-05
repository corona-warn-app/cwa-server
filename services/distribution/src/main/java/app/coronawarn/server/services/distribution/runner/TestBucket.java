package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.ObjectStore;
import java.net.URI;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

//@Component
//@Order(-2)
//@Profile("integration-test")
public class TestBucket implements ApplicationRunner {

  private final ObjectStore objectStoreConfig;

  public TestBucket(DistributionServiceConfig config) {
    this.objectStoreConfig = config.getObjectStore();
  }

  private static final Region DEFAULT_REGION = Region.EU_CENTRAL_1;

  private String removeTrailingSlash(String string) {
    return string.endsWith("/") ? string.substring(0, string.length() - 1) : string;
  }

  private S3Client createClient(ObjectStore objectStore) {
    AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
        AwsBasicCredentials.create(objectStore.getAccessKey(), objectStore.getSecretKey()));
    String endpoint = removeTrailingSlash(objectStore.getEndpoint()) + ":" + objectStore.getPort();

    return S3Client.builder()
        .region(DEFAULT_REGION)
        .endpointOverride(URI.create(endpoint))
        .credentialsProvider(credentialsProvider)
        .build();
  }

  @Override
  public void run(ApplicationArguments args) {
    var s3Client = this.createClient(objectStoreConfig);
    var request = CreateBucketRequest.builder()
        .bucket("cwa")
        .acl("public-read")
        .build();
    s3Client.createBucket(request);
  }

}
