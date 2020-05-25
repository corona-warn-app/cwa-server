/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.services.distribution.objectstore;


import static org.assertj.core.api.Assertions.assertThat;

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

    assertThat(s3Objects).hasSize(5);
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
