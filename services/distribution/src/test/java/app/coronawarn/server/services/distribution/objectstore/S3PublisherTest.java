package app.coronawarn.server.services.distribution.objectstore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import software.amazon.awssdk.services.s3.model.S3Object;

@SpringBootTest
@Tag("s3-integration")
public class S3PublisherTest {
  private static final Logger logger = LoggerFactory.getLogger(S3PublisherTest.class);

  private final String rootTestFolder = "objectstore/";

  @Autowired
  ObjectStoreAccess objectStoreAccess;

  @Test
  public void publishTestFolderOk() throws IOException {
    S3Publisher publisher = new S3Publisher(getFolderAsPath(rootTestFolder), objectStoreAccess, "publisher");

    printAllFiles();

    publisher.publish();

    printAllFiles();

    List<S3Object> s3Objects = objectStoreAccess.getObjectsWithPrefix("publisher")
        .collect(Collectors.toList());

    assertEquals(7, s3Objects.size());

  }

  private Path getFolderAsPath(String path) throws IOException {
    return Path.of(new ClassPathResource(path).getURI());
  }

  private void printAllFiles() {
    var out = objectStoreAccess.getObjectsWithPrefix("publisher");

    logger.info("-------");
    logger.info(out.collect(Collectors.toList()).toString());
    logger.info("-------");

    logger.info("Fetched S3");
  }

}
