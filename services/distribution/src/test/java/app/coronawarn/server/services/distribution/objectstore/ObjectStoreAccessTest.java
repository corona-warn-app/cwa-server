package app.coronawarn.server.services.distribution.objectstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

@SpringBootTest
@Tag("s3-integration")
public class ObjectStoreAccessTest {

  private String testRunId = "testing/cwa/" + UUID.randomUUID().toString() + "/";


  private String textFile = "objectstore/store-test-file";

  @Autowired
  private ObjectStoreAccess objectStoreAccess;

  @Test
  public void fetchFilesNotEmpty() {
    var files = objectStoreAccess.getFilesWithPrefix("");

    assertFalse(files.contents().isEmpty(), "Contents is empty, but we should have files");
  }

  @Test
  public void fetchFilesNothingFound() {
    var files = objectStoreAccess.getFilesWithPrefix("THISPREFIXDOESNOTEXIST");

    assertTrue(files.contents().isEmpty(), "Found files, but should be empty!");
  }

  @Test
  public void printFiles() {
    objectStoreAccess.printAllFiles();
  }

  @Test
  public void pushTestFileAndDelete() throws IOException {
    objectStoreAccess.put(testRunId + "TESTFILE", getExampleFile());
    var files = objectStoreAccess.getFilesWithPrefix(testRunId);
    assertEquals(1, files.contents().size());

    objectStoreAccess.printAllFiles();

    objectStoreAccess.deleteFilesWithPrefix(testRunId);
    var filesAfterDeletion = objectStoreAccess.getFilesWithPrefix(testRunId);
    assertEquals(0, filesAfterDeletion.contents().size());

    objectStoreAccess.printAllFiles();

    var allFiles = objectStoreAccess.getFilesWithPrefix("");
    assertFalse(allFiles.contents().isEmpty(), "Contents is empty, but we should have files");
  }

  private File getExampleFile() throws IOException {
    return new File(new ClassPathResource(textFile).getURI());
  }

}
