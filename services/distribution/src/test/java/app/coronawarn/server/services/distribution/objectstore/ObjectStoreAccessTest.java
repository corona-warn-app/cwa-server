package app.coronawarn.server.services.distribution.objectstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.objectstore.publish.LocalFile;
import app.coronawarn.server.services.distribution.objectstore.publish.LocalGenericFile;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

@SpringBootTest
@Tag("s3-integration")
public class ObjectStoreAccessTest {


  private static final Logger logger = LoggerFactory.getLogger(ObjectStoreAccessTest.class);

  private final String testRunId = "testing/cwa/" + UUID.randomUUID().toString() + "/";

  private final String textFile = "objectstore/store-test-file";

  private final String rootTestFolder = "objectstore/";

  @Autowired
  private ObjectStoreAccess objectStoreAccess;

  @Test
  public void fetchFilesNotEmpty() {
    var files = objectStoreAccess.getObjectsWithPrefix("");

    assertFalse(files.collect(Collectors.toList()).isEmpty(), "Contents is empty, but we should have files");
  }

  @Test
  public void fetchFilesNothingFound() {
    var files = objectStoreAccess.getObjectsWithPrefix("THIS_PREFIX_DOES_NOT_EXIST");

    assertTrue(files.collect(Collectors.toList()).isEmpty(), "Found files, but should be empty!");
  }

  @Test
  public void pushTestFileAndDelete() throws IOException {
    LocalFile localFile = new LocalGenericFile(getExampleFile(), getRootTestFolder());

    LocalFile localFileSpy = spy(localFile);
    when(localFileSpy.getS3Key()).thenReturn(testRunId + localFile.getS3Key());

    objectStoreAccess.putObject(localFileSpy);
    var files = objectStoreAccess.getObjectsWithPrefix(testRunId);
    assertEquals(1, files.collect(Collectors.toList()).size());

    this.printAllFiles();

    objectStoreAccess.deleteObjectsWithPrefix(testRunId);
    var filesAfterDeletion = objectStoreAccess.getObjectsWithPrefix(testRunId)
        .collect(Collectors.toList());
    assertEquals(0, filesAfterDeletion.size());

    this.printAllFiles();

    var allFiles = objectStoreAccess.getObjectsWithPrefix("").collect(Collectors.toList());
    assertFalse(allFiles.isEmpty(), "Contents is empty, but we should have files");
  }

  private Path getExampleFile() throws IOException {
    return Path.of(new ClassPathResource(textFile).getURI());
  }

  private Path getRootTestFolder() throws IOException {
    return Path.of(new ClassPathResource(rootTestFolder).getURI());
  }

  /**
   * Print some debug information about what is currently in the store.
   */
  private void printAllFiles() {
    var out = objectStoreAccess.getObjectsWithPrefix("");

    logger.info("-------");
    logger.info(out.collect(Collectors.toList()).toString());
    logger.info("-------");

    logger.info("Fetched S3");
  }
}
