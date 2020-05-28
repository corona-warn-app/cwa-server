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

import static java.util.Map.entry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.publish.LocalFile;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import java.io.File;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MinioClient.class}, initializers = ConfigFileApplicationContextInitializer.class)
class ObjectStoreAccessUnitTest {

  private static final String EXP_S3_KEY = "fooS3Key";
  private static final String EXP_FILE_CONTENT = "barFileContent";

  private final DistributionServiceConfig distributionServiceConfig;
  private final String expBucketName;
  private LocalFile testLocalFile;
  private ObjectStoreAccess objectStoreAccess;

  @MockBean
  private MinioClient minioClient;

  @Autowired
  public ObjectStoreAccessUnitTest(DistributionServiceConfig distributionServiceConfig) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.expBucketName = distributionServiceConfig.getObjectStore().getBucket();
  }

  @BeforeEach
  public void setUpMocks() throws Exception {
    when(minioClient.bucketExists(any())).thenReturn(true);
    this.objectStoreAccess = new ObjectStoreAccess(distributionServiceConfig, minioClient);
    this.testLocalFile = setUpLocalFileMock();
  }

  private LocalFile setUpLocalFileMock() {
    var testLocalFile = mock(LocalFile.class);
    var testPath = mock(Path.class);

    when(testLocalFile.getS3Key()).thenReturn(EXP_S3_KEY);
    when(testLocalFile.getFile()).thenReturn(testPath);
    when(testPath.toFile()).thenReturn(mock(File.class));
    when(testPath.toString()).thenReturn(EXP_FILE_CONTENT);

    return testLocalFile;
  }

  @Test
  void testPutObjectSetsDefaultCacheControlHeader() throws Exception {
    ArgumentCaptor<PutObjectOptions> options = ArgumentCaptor.forClass(PutObjectOptions.class);
    var expHeader = entry("cache-control", "public,max-age=" + ObjectStoreAccess.DEFAULT_MAX_CACHE_AGE);

    objectStoreAccess.putObject(testLocalFile);

    verify(minioClient, atLeastOnce())
        .putObject(eq(expBucketName), eq(EXP_S3_KEY), eq(EXP_FILE_CONTENT), options.capture());
    Assertions.assertThat(options.getValue().headers()).contains(expHeader);
  }

  @Test
  void testPutObjectSetsSpecifiedCacheControlHeader() throws Exception {
    ArgumentCaptor<PutObjectOptions> options = ArgumentCaptor.forClass(PutObjectOptions.class);
    var expMaxAge = 1337;
    var expHeader = entry("cache-control", "public,max-age=" + expMaxAge);

    objectStoreAccess.putObject(testLocalFile, expMaxAge);

    verify(minioClient, atLeastOnce())
        .putObject(eq(expBucketName), eq(EXP_S3_KEY), eq(EXP_FILE_CONTENT), options.capture());
    Assertions.assertThat(options.getValue().headers()).contains(expHeader);
  }
}
