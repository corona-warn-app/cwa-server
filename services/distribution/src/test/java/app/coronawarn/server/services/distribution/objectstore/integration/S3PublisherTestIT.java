

package app.coronawarn.server.services.distribution.objectstore.integration;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.FailedObjectStoreOperationsCounter;
import app.coronawarn.server.services.distribution.objectstore.ObjectStoreAccess;
import app.coronawarn.server.services.distribution.objectstore.S3Publisher;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStorePublishingConfig;
import app.coronawarn.server.services.distribution.objectstore.client.S3Object;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ObjectStoreAccess.class, ObjectStorePublishingConfig.class, S3Publisher.class})
@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@DirtiesContext
@Tag("s3-integration")
class S3PublisherTestIT {

  private final String rootTestFolder = "objectstore/publisher/";

  @Autowired
  private ObjectStoreAccess objectStoreAccess;

  @Autowired
  private ResourceLoader resourceLoader;

  @MockBean
  private FailedObjectStoreOperationsCounter failedObjectStoreOperationsCounter;

  @Autowired
  private S3Publisher s3Publisher;


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

  @BeforeEach
  public void setup() {
    objectStoreAccess.deleteObjectsWithPrefix("");
  }

  @AfterEach
  public void teardown() {
    objectStoreAccess.deleteObjectsWithPrefix("");
  }

  @Test
  void publishTestFolderOk() throws IOException {
    s3Publisher.publish(getFolderAsPath(rootTestFolder));
    List<S3Object> s3Objects = objectStoreAccess.getObjectsWithPrefix("version");

    assertThat(s3Objects).hasSize(5);
  }

  private Path getFolderAsPath(String path) throws IOException {
    return resourceLoader.getResource(path).getFile().toPath();
  }


}
