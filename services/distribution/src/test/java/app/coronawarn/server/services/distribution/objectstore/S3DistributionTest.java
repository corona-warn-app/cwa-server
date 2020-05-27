package app.coronawarn.server.services.distribution.objectstore;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.services.distribution.Application;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.runner.S3Distribution;
import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.MinioException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Application.class},
    initializers = ConfigFileApplicationContextInitializer.class)
@TestInstance(Lifecycle.PER_CLASS)
public class S3DistributionTest {

  private static final String textFile = "objectstore/store-test-file";

  private MinioClient client;

  @Autowired
  private DistributionServiceConfig distributionServiceConfig;

  @Autowired
  private ObjectStoreAccess objectStoreAccess;

  @Autowired
  private S3Distribution s3Distribution;

  @Autowired
  private ResourceLoader resourceLoader;

  @Test
  void shouldDeleteOldFiles() throws IOException, GeneralSecurityException, MinioException {
    this.client.putObject(this.distributionServiceConfig.getObjectStore().getBucket(),
        "version/v1/diagnosis-keys/country/DE/date/1970-01-01/hour/0", getExampleFilePath(), null);
    this.client.putObject(this.distributionServiceConfig.getObjectStore().getBucket(),
        "version/v1/diagnosis-keys/country/DE/date/" + LocalDate.now().toString() + "/hour/0", getExampleFilePath(),
        null);
    this.client.putObject(this.distributionServiceConfig.getObjectStore().getBucket(),
        "version/v1/configuration/country/DE/app_config", getExampleFilePath(), null);

    assertThat(objectStoreAccess.getObjectsWithPrefix("version/v1/").size()).isEqualTo(3);

    s3Distribution.applyRetentionPolicy(distributionServiceConfig.getRetentionDays());

    assertThat(objectStoreAccess.getObjectsWithPrefix("version/v1/").size()).isEqualTo(2);
  }

  private String getExampleFilePath() throws IOException {
    return resourceLoader.getResource(textFile).getFile().toString();
  }

  @BeforeAll
  public void connectS3Storage() throws InvalidPortException, InvalidEndpointException {
    this.client = new MinioClient(
        distributionServiceConfig.getObjectStore().getEndpoint(),
        distributionServiceConfig.getObjectStore().getPort(),
        distributionServiceConfig.getObjectStore().getAccessKey(),
        distributionServiceConfig.getObjectStore().getSecretKey()
    );
  }

  @BeforeEach
  public void setup()
      throws MinioException, GeneralSecurityException, IOException {
    objectStoreAccess.deleteObjectsWithPrefix("");
  }

  @AfterEach
  public void teardown() throws IOException, GeneralSecurityException, MinioException {
    objectStoreAccess.deleteObjectsWithPrefix("");
  }
}
