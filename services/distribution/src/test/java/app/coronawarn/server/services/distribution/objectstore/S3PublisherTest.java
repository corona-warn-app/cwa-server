package app.coronawarn.server.services.distribution.objectstore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import app.coronawarn.server.services.distribution.Application;
import io.minio.errors.MinioException;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Application.class},
    initializers = ConfigFileApplicationContextInitializer.class)
@Tag("s3-integration")
public class S3PublisherTest {

  private final String rootTestFolder = "objectstore/publisher/";

  @Autowired
  private ObjectStoreAccess objectStoreAccess;

  @Autowired
  private ResourceLoader resourceLoader;

  @Test
  public void publishTestFolderOk() throws IOException, GeneralSecurityException, MinioException {
    S3Publisher publisher = new S3Publisher(getFolderAsPath(rootTestFolder), objectStoreAccess);

    publisher.publish();

    List<S3Object> s3Objects = objectStoreAccess.getObjectsWithPrefix("version");

    assertEquals(5, s3Objects.size());
  }

  private Path getFolderAsPath(String path) throws IOException {
    return resourceLoader.getResource(path).getFile().toPath();
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
