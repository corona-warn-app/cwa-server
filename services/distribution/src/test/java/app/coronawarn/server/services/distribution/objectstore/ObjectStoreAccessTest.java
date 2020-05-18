package app.coronawarn.server.services.distribution.objectstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.Application;
import app.coronawarn.server.services.distribution.objectstore.publish.LocalFile;
import app.coronawarn.server.services.distribution.objectstore.publish.LocalGenericFile;
import io.minio.errors.MinioException;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Application.class},
    initializers = ConfigFileApplicationContextInitializer.class)
@Tag("s3-integration")
public class ObjectStoreAccessTest {

  private static final String testRunId = "testing/cwa/" + UUID.randomUUID().toString() + "/";

  private static final String rootTestFolder = "objectstore/";

  private static final String textFile = rootTestFolder + "store-test-file";

  @Autowired
  private ObjectStoreAccess objectStoreAccess;

  @BeforeEach
  public void setup()
      throws MinioException, GeneralSecurityException, IOException {
    objectStoreAccess.deleteObjectsWithPrefix(testRunId);
  }

  @AfterEach
  public void teardown() throws IOException, GeneralSecurityException, MinioException {
    objectStoreAccess.deleteObjectsWithPrefix(testRunId);
  }

  @Test
  public void defaultIsEmptyTrue() throws MinioException, GeneralSecurityException, IOException {
    var files = objectStoreAccess.getObjectsWithPrefix(testRunId);

    assertTrue(files.isEmpty(), "Content should be empty");
  }

  @Test
  public void fetchFilesNothingFound()
      throws MinioException, GeneralSecurityException, IOException {
    var files = objectStoreAccess.getObjectsWithPrefix("THIS_PREFIX_DOES_NOT_EXIST");

    assertTrue(files.isEmpty(), "Found files, but should be empty!");
  }

  @Test
  public void pushTestFileAndDelete() throws IOException, GeneralSecurityException, MinioException {
    LocalFile localFile = new LocalGenericFile(getExampleFile(), getRootTestFolder());
    String testFileTargetKey = testRunId + localFile.getS3Key();

    LocalFile localFileSpy = spy(localFile);
    when(localFileSpy.getS3Key()).thenReturn(testRunId + localFile.getS3Key());

    objectStoreAccess.putObject(localFileSpy);
    var files = objectStoreAccess.getObjectsWithPrefix(testRunId);
    assertEquals(1, files.size());

    assertEquals(testFileTargetKey, files.get(0).getObjectName());

    objectStoreAccess.deleteObjectsWithPrefix(testRunId);

    var filesAfterDeletion = objectStoreAccess.getObjectsWithPrefix(testRunId);
    assertEquals(0, filesAfterDeletion.size());
  }

  private Path getExampleFile() throws IOException {
    return Path.of(new ClassPathResource(textFile).getURI());
  }

  private Path getRootTestFolder() throws IOException {
    return Path.of(new ClassPathResource(rootTestFolder).getURI());
  }

}
