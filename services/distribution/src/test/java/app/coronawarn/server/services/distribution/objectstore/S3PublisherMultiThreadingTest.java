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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreClient;
import app.coronawarn.server.services.distribution.util.AsyncConfiguration;
import app.coronawarn.server.services.distribution.util.S3UploadDaemonThreadFactory;
import java.io.IOException;
import java.util.concurrent.Executor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class})
@SpringBootTest(classes = {AsyncConfiguration.class, S3UploadDaemonThreadFactory.class})
@EnableConfigurationProperties(value = DistributionServiceConfig.class)
class S3PublisherMultiThreadingTest {

  private static final String PUBLISHING_PATH = "testsetups/s3publishertest/topublish";
  private ObjectStoreAccess objectStoreAccess;

  @MockBean
  private ObjectStoreClient objectStoreClient;

  @Autowired
  private ResourceLoader resourceLoader;

  @Autowired
  private AsyncConfiguration asyncConfiguration;

  @Autowired
  private S3UploadDaemonThreadFactory s3UploadDaemonThreadFactory;

  @Autowired
  private DistributionServiceConfig distributionServiceConfig;

  @BeforeAll
  public static void setup() {
    Configurator.setLevel("app.coronawarn.server.services.distribution.objectstore", Level.INFO);
  }

  @AfterAll
  public static void teardown() {
    Configurator.setLevel("app.coronawarn.server.services.distribution.objectstore", Level.OFF);
  }

  @BeforeEach
  public void setUpMocks() {
    when(objectStoreClient.bucketExists(any())).thenReturn(true);
    this.objectStoreAccess = new ObjectStoreAccess(distributionServiceConfig, objectStoreClient);
  }

  @Test
  @ExtendWith(OutputCaptureExtension.class)
  void shouldRunMultiThreaded(CapturedOutput output) throws IOException {
    var publishPath = resourceLoader.getResource(PUBLISHING_PATH).getFile().toPath();
    new S3Publisher(publishPath, objectStoreAccess, mock(FailedObjectStoreOperationsCounter.class)).publish();

    assertThat(output).contains("s3Op-0");
    assertThat(output).contains("s3Op-1");
    assertThat(output).doesNotContain("s3Op-2");
  }

  @Test
  void threadFactoryShouldBeCorrectlyInstantiated() {
    Executor executor = asyncConfiguration.s3TaskExecutor();
    assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
  }

  @Test
  void testS3UploadDaemonThreadFactory() {
    Thread createdThread = s3UploadDaemonThreadFactory.newThread(() -> {
    });
    assertThat(createdThread.getName()).contains("s3Op");
    assertThat(createdThread.isDaemon()).isEqualTo(true);
  }

}
