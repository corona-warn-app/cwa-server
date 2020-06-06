/*
 * -
 *  * ---license-start
 *  * Corona-Warn-App
 *  * ---
 *  * Copyright (C) 2020 SAP SE and all other contributors
 *  * ---
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  * ---license-end
 *
 */

package app.coronawarn.server.services.distribution.objectstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreClientConfig;
import app.coronawarn.server.services.distribution.util.AsyncConfiguration;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class})
@SpringBootTest(classes = {ObjectStoreAccess.class, ObjectStoreClientConfig.class, AsyncConfiguration.class})
@EnableConfigurationProperties(value = DistributionServiceConfig.class)
public class S3PublisherMultiThreadingTest {

  private static final String PUBLISHING_PATH = "testsetups/s3publishertest/topublish";

  @Autowired
  private ObjectStoreAccess objectStoreAccess;

  @Autowired
  private ResourceLoader resourceLoader;

  @Test
  @ExtendWith(OutputCaptureExtension.class)
  void shouldRunMultiThreaded(CapturedOutput output) throws IOException {
    createPublisher().publish();
    assertThat(output).contains("Thread-0");
    assertThat(output).contains("Thread-1");
  }

  private S3Publisher createPublisher() throws IOException {
    var publishPath = resourceLoader.getResource(PUBLISHING_PATH).getFile().toPath();
    return new S3Publisher(publishPath, objectStoreAccess, mock(FailedObjectStoreOperationsCounter.class));
  }

}
