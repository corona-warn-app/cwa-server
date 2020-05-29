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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.minio.errors.MinioException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class S3PublisherTest {

  private static final String PUBLISHING_PATH = "testsetups/s3publishertest/topublish";
  private static final S3Object FILE_1 = new S3Object("file1.txt", "39f7e3afaa8f8560a3050d1a1b365f47-1");
  private static final S3Object FILE_2 = new S3Object("file2.txt", "8d29190901bfde9710ea76b29ef3d33e-1");
  private static final S3Object FILE_3 = new S3Object("file3.txt", "585f3b3f71f6b1519a21bef8bb77cf01-1");

  @MockBean
  private ObjectStoreAccess objectStoreAccess;

  @Autowired
  private ResourceLoader resourceLoader;

  @Test
  void allNewNoExisting() throws IOException, GeneralSecurityException, MinioException {
    when(objectStoreAccess.getObjectsWithPrefix("version")).thenReturn(noneExisting());

    createTestPublisher().publish();

    verify(objectStoreAccess, times(3)).putObject(any());
  }

  @Test
  void noUploadsDueToAlreadyExist() throws IOException, GeneralSecurityException, MinioException {
    when(objectStoreAccess.getObjectsWithPrefix("version")).thenReturn(allExistAllSame());

    createTestPublisher().publish();

    verify(objectStoreAccess, times(0)).putObject(any());
  }

  @Test
  void uploadAllOtherFilesDifferentNames() throws IOException, GeneralSecurityException, MinioException {
    when(objectStoreAccess.getObjectsWithPrefix("version")).thenReturn(otherExisting());

    createTestPublisher().publish();

    verify(objectStoreAccess, times(3)).putObject(any());
  }

  @Test
  void uploadOneDueToOneChanged() throws IOException, GeneralSecurityException, MinioException {
    when(objectStoreAccess.getObjectsWithPrefix("version")).thenReturn(twoIdenticalOneOtherOneChange());

    createTestPublisher().publish();

    verify(objectStoreAccess, times(1)).putObject(any());
  }

  private List<S3Object> noneExisting() {
    return List.of();
  }

  private List<S3Object> otherExisting() {
    return List.of(
        new S3Object("some_old_file.txt", "1fb772815c837b6294d9f163db89e962-1"),
        new S3Object("other_old_file.txt", "2fb772815c837b6294d9f163db89e962-1")
    );
  }

  private List<S3Object> allExistAllSame() {
    return List.of(
        FILE_1,
        FILE_2,
        FILE_3
    );
  }

  private List<S3Object> twoIdenticalOneOtherOneChange() {
    return List.of(
        new S3Object("newfile.txt", "1fb772815c837b6294d9f163db89e962-1"), // new
        FILE_1,
        FILE_2,
        new S3Object("file3.txt", "111772815c837b6294d9f163db89e962-1") // changed
    );
  }

  private S3Publisher createTestPublisher() throws IOException {
    var publishPath = resourceLoader.getResource(PUBLISHING_PATH).getFile().toPath();
    return new S3Publisher(publishPath, objectStoreAccess);
  }
}
