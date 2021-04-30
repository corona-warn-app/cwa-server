package app.coronawarn.server.services.distribution.objectstore.integration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.S3ClientOptions;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseS3IntegrationTest {

  @BeforeAll
  public static void setupBucket() {
    AWSCredentials credentials = new BasicAWSCredentials("accessKey1", "verySecretKey1");
    AmazonS3 s3client = createS3Client(credentials);
    createBucketIfNotExists(s3client);
  }

  private static AmazonS3 createS3Client(AWSCredentials credentials) {
    AmazonS3 s3client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
        .build();
    s3client.setEndpoint("http://localhost:8003");
    s3client.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).build());
    return s3client;
  }

  private static void createBucketIfNotExists(AmazonS3 s3client) {
    String bucketName = "cwa";
    if (!s3client.doesBucketExistV2(bucketName)) {
      s3client.createBucket(bucketName);
    }
  }
}
