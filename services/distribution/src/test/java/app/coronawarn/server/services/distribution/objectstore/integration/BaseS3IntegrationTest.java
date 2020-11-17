package app.coronawarn.server.services.distribution.objectstore.integration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseS3IntegrationTest {

  @BeforeAll
  public static void setupBucket() {
    AWSCredentials credentials = new BasicAWSCredentials("accessKey1",
        "verySecretKey1");

    // Create a client connection based on credentials
    AmazonS3 s3client = new AmazonS3Client(credentials);
    s3client.setEndpoint("http://localhost:8003");
    s3client.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).build());

    // Create bucket
    String bucketName = "cwa";
    if (!s3client.doesBucketExistV2(bucketName)) {
      s3client.createBucket(bucketName);
    }
  }

}
