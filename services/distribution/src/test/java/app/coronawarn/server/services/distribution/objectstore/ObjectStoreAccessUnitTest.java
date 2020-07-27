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

import static java.util.Map.entry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreClient;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreClient.HeaderKey;
import app.coronawarn.server.services.distribution.objectstore.publish.LocalFile;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;
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
@ContextConfiguration(classes = {ObjectStoreClient.class}, initializers = ConfigFileApplicationContextInitializer.class)
class ObjectStoreAccessUnitTest {

  private static final String EXP_S3_KEY = "fooS3Key";

  private final DistributionServiceConfig distributionServiceConfig;
  private final String expBucketName;
  private LocalFile testLocalFile;
  private ObjectStoreAccess objectStoreAccess;
  private Path expPath;

  @MockBean
  private ObjectStoreClient objectStoreClient;

  @Autowired
  public ObjectStoreAccessUnitTest(DistributionServiceConfig distributionServiceConfig) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.expBucketName = distributionServiceConfig.getObjectStore().getBucket();
  }

  @BeforeEach
  public void setUpMocks() {
    when(objectStoreClient.bucketExists(any())).thenReturn(true);
    this.objectStoreAccess = new ObjectStoreAccess(distributionServiceConfig, objectStoreClient);
    this.testLocalFile = setUpLocalFileMock();
  }

  private LocalFile setUpLocalFileMock() {
    var testLocalFile = mock(LocalFile.class);
    expPath = mock(Path.class);

    when(testLocalFile.getS3Key()).thenReturn(EXP_S3_KEY);
    when(testLocalFile.getFile()).thenReturn(expPath);
    when(expPath.toFile()).thenReturn(mock(File.class));

    return testLocalFile;
  }

  @Test
  void testPutObjectSetsDefaultCacheControlHeader() {
    ArgumentCaptor<Map<HeaderKey, String>> headers = ArgumentCaptor.forClass(Map.class);
    var expHeader = entry(HeaderKey.CACHE_CONTROL, "public,max-age=" + ObjectStoreAccess.DEFAULT_MAX_CACHE_AGE);

    objectStoreAccess.putObject(testLocalFile);

    verify(objectStoreClient, atLeastOnce())
        .putObject(eq(expBucketName), eq(EXP_S3_KEY), eq(expPath), headers.capture());
    Assertions.assertThat(headers.getValue()).contains(expHeader);
  }

  @Test
  void testPutObjectSetsSpecifiedCacheControlHeader() {
    ArgumentCaptor<Map<HeaderKey, String>> headers = ArgumentCaptor.forClass(Map.class);
    var expMaxAge = 1337;
    var expHeader = entry(HeaderKey.CACHE_CONTROL, "public,max-age=" + expMaxAge);

    objectStoreAccess.putObject(testLocalFile, expMaxAge);

    verify(objectStoreClient, atLeastOnce())
        .putObject(eq(expBucketName), eq(EXP_S3_KEY), eq(expPath), headers.capture());
    Assertions.assertThat(headers.getValue()).contains(expHeader);
  }
}
