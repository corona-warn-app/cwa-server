package app.coronawarn.server.services.distribution.objectstore;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

@SpringBootTest
@Tag("s3-integration")
@TestInstance(Lifecycle.PER_CLASS)
public class S3PublisherTest {

  private final String testRunId = "testing/cwa/" + UUID.randomUUID().toString() + "/";

  private final String rootTestFolder = "objectstore/publisher/";

  private final String exampleFile = rootTestFolder + "rootfile";

  @Autowired
  private S3Publisher s3Publisher;

  @Test
  public void publishFolder() throws IOException {
    Path start = Paths.get(getFile(rootTestFolder).getPath());

    s3Publisher.publishFolder(start);
  }

  @Test
  public void publishSingleFile() throws IOException {
    Path fileToPublish = getFile(exampleFile).toPath();
    Path path = getFile(rootTestFolder).toPath();

    s3Publisher.publishFile(fileToPublish, path);
    assertTrue(s3Publisher.isFileExisting(fileToPublish, path), "File should exist on S3");

    s3Publisher.deleteFile(fileToPublish, path);
    assertFalse(s3Publisher.isFileExisting(fileToPublish, path), "File should have been deleted");
  }

  private File getFile(String path) throws IOException {
    return new File(new ClassPathResource(path).getURI());
  }

  @BeforeAll
  public void setup() {
    s3Publisher.setPrefixPath(this.testRunId);
  }

  @AfterAll
  public void teardown() {
    s3Publisher.deleteFolder("");

    s3Publisher.setPrefixPath("cwa/");
  }
}
