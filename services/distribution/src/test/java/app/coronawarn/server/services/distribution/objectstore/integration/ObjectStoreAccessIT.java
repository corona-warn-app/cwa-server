

package app.coronawarn.server.services.distribution.objectstore.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.ObjectStoreAccess;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStorePublishingConfig;
import app.coronawarn.server.services.distribution.objectstore.client.S3Object;
import app.coronawarn.server.services.distribution.objectstore.publish.LocalFile;
import app.coronawarn.server.services.distribution.objectstore.publish.LocalGenericFile;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ObjectStoreAccess.class, ObjectStorePublishingConfig.class})
@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@DirtiesContext
@Tag("s3-integration")
@ActiveProfiles("integration-test")
class ObjectStoreAccessIT {

  private static final String testRunId = "testing/cwa/" + UUID.randomUUID().toString() + "/";
  private static final String rootTestFolder = "objectstore/";
  private static final String textFile = rootTestFolder + "store-test-file";

  @Autowired
  private ObjectStoreAccess objectStoreAccess;

  @Autowired
  private ResourceLoader resourceLoader;

  @Autowired
  private ObjectStorePublishingConfig objectStorePublishingConfig;

  @BeforeAll
  public static void setupBucket() {
    AWSCredentials credentials = new BasicAWSCredentials("accessKey1",
        "verySecretKey1");

    // Create a client connection based on credentials
    AmazonS3 s3client = new AmazonS3Client(credentials);
    s3client.setEndpoint("http://localhost:8003");
    // Using path-style requests
    // (deprecated) s3client.setS3ClientOptions(new S3ClientOptions().withPathStyleAccess(true));
    s3client.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).build());

    // Create bucket
    String bucketName = "cwa";
    if (!s3client.doesBucketExistV2(bucketName)) {
      s3client.createBucket(bucketName);
    }
  }

  @BeforeEach
  public void setup() {
    objectStoreAccess.deleteObjectsWithPrefix(testRunId);
  }

  @AfterEach
  public void teardown() {
    objectStoreAccess.deleteObjectsWithPrefix(testRunId);
  }

  @Test
  public void contextLoads() throws Exception {
    assertThat(objectStoreAccess).isNotNull();
  }

  @Test
  void defaultIsEmptyTrue() {
    var files = objectStoreAccess.getObjectsWithPrefix(testRunId);

    assertThat(files).withFailMessage("Content should be empty").isEmpty();
  }

  @Test
  void fetchFilesNothingFound() {
    var files = objectStoreAccess.getObjectsWithPrefix("THIS_PREFIX_DOES_NOT_EXIST");

    assertThat(files).withFailMessage("Found files, but should be empty!").isEmpty();
  }

  @Test
  void pushTestFileAndDelete() throws IOException {
    LocalFile localFile = new LocalGenericFile(getExampleFile(), getRootTestFolder());
    String testFileTargetKey = testRunId + localFile.getS3Key();

    LocalFile localFileSpy = spy(localFile);
    when(localFileSpy.getS3Key()).thenReturn(testRunId + localFile.getS3Key());

    objectStoreAccess.putObject(localFileSpy);
    List<S3Object> files = objectStoreAccess.getObjectsWithPrefix(testRunId);
    assertThat(files).hasSize(1);

    assertThat(files.get(0).getObjectName()).isEqualTo(testFileTargetKey);

    objectStoreAccess.deleteObjectsWithPrefix(testRunId);

    List<S3Object> filesAfterDeletion = objectStoreAccess.getObjectsWithPrefix(testRunId);
    assertThat(filesAfterDeletion).isEmpty();
  }

  private Path getExampleFile() throws IOException {
    return resourceLoader.getResource(textFile).getFile().toPath();
  }

  private Path getRootTestFolder() throws IOException {
    return resourceLoader.getResource(rootTestFolder).getFile().toPath();
  }
}
