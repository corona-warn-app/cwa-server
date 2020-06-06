/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.distribution.objectstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreClientConfig;
import app.coronawarn.server.services.distribution.objectstore.publish.LocalFile;
import app.coronawarn.server.services.distribution.objectstore.publish.LocalGenericFile;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ObjectStoreAccess.class, ObjectStoreClientConfig.class})
@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@Tag("s3-integration")
class ObjectStoreAccessTest {

  private static final String testRunId = "testing/cwa/" + UUID.randomUUID().toString() + "/";

  private static final String rootTestFolder = "objectstore/";

  private static final String textFile = rootTestFolder + "store-test-file";

  @Autowired
  private ObjectStoreAccess objectStoreAccess;

  @Autowired
  private ResourceLoader resourceLoader;

  @BeforeEach
  public void setup() {
    objectStoreAccess.deleteObjectsWithPrefix(testRunId);
  }

  @AfterEach
  public void teardown() {
    objectStoreAccess.deleteObjectsWithPrefix(testRunId);
  }

  @Test
  void defaultIsEmptyTrue() {
    var files = objectStoreAccess.getObjectsWithPrefix(testRunId);

    assertThat(files).withFailMessage("Content should be empty").isEmpty();
  }

  @Test
  void fetchFilesNothingFound() {
    var files = objectStoreAccess.getObjectsWithPrefix("THIS_PREFIX_DOES_NOT_EXIST");

    assertThat(files).withFailMessage("Found files, but should be empty!").isEmpty();
  }

  @Test
  void pushTestFileAndDelete() throws IOException {
    LocalFile localFile = new LocalGenericFile(getExampleFile(), getRootTestFolder());
    String testFileTargetKey = testRunId + localFile.getS3Key();

    LocalFile localFileSpy = spy(localFile);
    when(localFileSpy.getS3Key()).thenReturn(testRunId + localFile.getS3Key());

    objectStoreAccess.putObject(localFileSpy);
    var files = objectStoreAccess.getObjectsWithPrefix(testRunId);
    assertThat(files.size()).isEqualTo(1);

    assertThat(files.get(0).getObjectName()).isEqualTo(testFileTargetKey);

    objectStoreAccess.deleteObjectsWithPrefix(testRunId);

    var filesAfterDeletion = objectStoreAccess.getObjectsWithPrefix(testRunId);
    assertThat(filesAfterDeletion.size()).isEqualTo(0);
  }

  private Path getExampleFile() throws IOException {
    return resourceLoader.getResource(textFile).getFile().toPath();
  }

  private Path getRootTestFolder() throws IOException {
    return resourceLoader.getResource(rootTestFolder).getFile().toPath();
  }

}
