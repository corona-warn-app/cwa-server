package app.coronawarn.server.services.distribution.objectstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
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


  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final String testRunId = "testing/cwa/" + UUID.randomUUID().toString() + "/";

  private final String textFile = "objectstore/store-test-file";

  @Autowired
  private ObjectStoreAccess objectStoreAccess;

  @Test
  public void fetchFilesNotEmpty() {
    var files = objectStoreAccess.getObjectsWithPrefix("");

    assertFalse(files.contents().isEmpty(), "Contents is empty, but we should have files");
  }

  @Test
  public void fetchFilesNothingFound() {
    var files = objectStoreAccess.getObjectsWithPrefix("THISPREFIXDOESNOTEXIST");

    assertTrue(files.contents().isEmpty(), "Found files, but should be empty!");
  }

  @Test
  public void pushTestFileAndDelete() throws IOException {
    objectStoreAccess.putObject(testRunId + "TESTFILE", getExampleFile());
    var files = objectStoreAccess.getObjectsWithPrefix(testRunId);
    assertEquals(1, files.contents().size());

    this.printAllFiles();

    objectStoreAccess.deleteObjectsWithPrefix(testRunId);
    var filesAfterDeletion = objectStoreAccess.getObjectsWithPrefix(testRunId);
    assertEquals(0, filesAfterDeletion.contents().size());

    this.printAllFiles();

    var allFiles = objectStoreAccess.getObjectsWithPrefix("");
    assertFalse(allFiles.contents().isEmpty(), "Contents is empty, but we should have files");
  }

  private File getExampleFile() throws IOException {
    return new File(new ClassPathResource(textFile).getURI());
  }

  /**
   * Print some debug information about what is currently in the store.
   */
  private void printAllFiles() {
    var out = objectStoreAccess.getObjectsWithPrefix("");

    logger.info("-------");
    logger.info(out.contents().toString());
    logger.info("-------");

    logger.info("Fetched S3");
  }
}
