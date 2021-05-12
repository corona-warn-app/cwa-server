package app.coronawarn.server.services.distribution.objectstore.integration;

import app.coronawarn.server.services.distribution.Application;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreClient;
import app.coronawarn.server.services.distribution.objectstore.client.S3ClientWrapper;
import java.net.URI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.vault.config.VaultAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;


@ActiveProfiles("integration-test")
@ContextConfiguration(classes = {Application.class,
    BaseS3IntegrationTest.S3IntegrationConfiguration.class}, initializers = ConfigDataApplicationContextInitializer.class)
@DirtiesContext
@EnableAutoConfiguration(exclude = VaultAutoConfiguration.class)
@ExtendWith(SpringExtension.class)
@Tag("s3-integration")
@Testcontainers
public abstract class BaseS3IntegrationTest {

  @Container
  public static final GenericContainer cloudServer = new GenericContainer(DockerImageName.parse("zenko/cloudserver"))
      .withExposedPorts(8000)
      .withEnv("REMOTE_MANAGEMENT_DISABLE", "1")
      .withEnv("S3BACKEND", "mem");

  public static final String DEFAULT_ACCESS_KEY = "accessKey1";
  public static final String DEFAULT_SECRET_KEY = "verySecretKey1";
  public static final AwsCredentials DEFAULT_CREDENTIALS = AwsBasicCredentials
      .create(DEFAULT_ACCESS_KEY, DEFAULT_SECRET_KEY);

  public static S3Client DEFAULT_CLIENT;


  @TestConfiguration
  public static class S3IntegrationConfiguration {

    @Bean(name = "publish-s3")
    @Primary
    @Profile("integration-test")
    public ObjectStoreClient getS3Client() {
      return new S3ClientWrapper(DEFAULT_CLIENT);
    }
  }

  @BeforeAll
  public static void setupBucket() {
    DEFAULT_CLIENT = S3Client.builder()
        .region(Region.EU_WEST_1)
        .credentialsProvider(StaticCredentialsProvider.create(DEFAULT_CREDENTIALS))
        .endpointOverride(URI.create("http://" + cloudServer.getHost() + ":" + cloudServer.getFirstMappedPort()))
        .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
        .build();
    createBucketIfNotExists();
  }

  private static void createBucketIfNotExists() {
    String bucketName = "cwa";
    final HeadBucketRequest checkIfBucketExistsRequest = HeadBucketRequest.builder()
        .bucket(bucketName).build();
    try {
      DEFAULT_CLIENT.headBucket(checkIfBucketExistsRequest);
    } catch (NoSuchBucketException e) {
      DEFAULT_CLIENT.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
      DEFAULT_CLIENT.waiter()
          .waitUntilBucketExists(checkIfBucketExistsRequest);
      DEFAULT_CLIENT.waiter().waitUntilBucketExists(checkIfBucketExistsRequest);
    }
  }
}
