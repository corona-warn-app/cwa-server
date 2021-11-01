

package app.coronawarn.server.services.distribution.objectstore.integration;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.services.distribution.objectstore.FailedObjectStoreOperationsCounter;
import app.coronawarn.server.services.distribution.objectstore.ObjectStoreAccess;
import app.coronawarn.server.services.distribution.objectstore.S3Publisher;
import app.coronawarn.server.services.distribution.objectstore.client.S3Object;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ResourceLoader;

class S3PublisherTestIT extends BaseS3IntegrationTest {

  private final String rootTestFolder = "objectstore/publisher/";

  @Autowired
  private ObjectStoreAccess objectStoreAccess;

  @Autowired
  private ResourceLoader resourceLoader;

  @MockBean
  private FailedObjectStoreOperationsCounter failedObjectStoreOperationsCounter;

  @Autowired
  private S3Publisher s3Publisher;

  @BeforeEach
  public void setup() {
    objectStoreAccess.deleteObjectsWithPrefix("");
  }

  @Test
  void publishTestFolderOk() throws IOException {
    s3Publisher.publish(getFolderAsPath(rootTestFolder));
    List<S3Object> s3Objects = objectStoreAccess.getObjectsWithPrefix("version");

    assertThat(s3Objects).hasSize(6);
  }

  private Path getFolderAsPath(String path) throws IOException {
    return resourceLoader.getResource(path).getFile().toPath();
  }


}
