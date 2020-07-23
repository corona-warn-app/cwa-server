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

import static org.assertj.core.util.Lists.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.Api;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreOperationFailedException;
import app.coronawarn.server.services.distribution.objectstore.client.S3Object;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Api.class}, initializers = ConfigFileApplicationContextInitializer.class)
class S3PublisherTest {

  private static final S3Object FILE_1 = new S3Object("file1.txt", "cf7fb1ca5c32adc0941c35a6f7fc5eba");
  private static final S3Object FILE_2 = new S3Object("file2.txt", "d882afb9fa9c26f7e9d0965b8faa79b8");
  private static final S3Object FILE_3 = new S3Object("file3.txt", "0385524c9fdc83634467a11667c851ac");

  @MockBean
  private ObjectStoreAccess objectStoreAccess;

  @MockBean
  private FailedObjectStoreOperationsCounter failedObjectStoreOperationsCounter;

  @Autowired
  private ResourceLoader resourceLoader;

  @Autowired
  private DistributionServiceConfig distributionServiceConfig;

  private ThreadPoolTaskExecutor executor;
  private Path publishingPath;
  private S3Publisher s3Publisher;

  @BeforeEach
  void setup() throws IOException {
    publishingPath = resourceLoader.getResource("testsetups/s3publishertest/topublish").getFile().toPath();
    executor = new ThreadPoolTaskExecutor();
    executor.setMaxPoolSize(1);
    executor.setCorePoolSize(1);
    executor.initialize();
    executor = spy(executor);
    s3Publisher = new S3Publisher(objectStoreAccess, failedObjectStoreOperationsCounter, executor,
        distributionServiceConfig);
  }

  @Test
  void allNewNoExisting() throws IOException {
    when(objectStoreAccess.getObjectsWithPrefix("version")).thenReturn(emptyList());

    s3Publisher.publish(publishingPath);

    verify(objectStoreAccess, times(3)).putObject(any());
  }

  @Test
  void noUploadsDueToAlreadyExist() throws IOException {
    when(objectStoreAccess.getObjectsWithPrefix("version")).thenReturn(allExistAllSame());

    s3Publisher.publish(publishingPath);

    verify(objectStoreAccess, times(0)).putObject(any());
  }

  @Test
  void uploadAllOtherFilesDifferentNames() throws IOException {
    when(objectStoreAccess.getObjectsWithPrefix("version")).thenReturn(otherExisting());

    s3Publisher.publish(publishingPath);

    verify(objectStoreAccess, times(3)).putObject(any());
  }

  @Test
  void uploadOneDueToOneChanged() throws IOException {
    when(objectStoreAccess.getObjectsWithPrefix("version")).thenReturn(twoIdenticalOneOtherOneChange());

    s3Publisher.publish(publishingPath);

    verify(objectStoreAccess, times(1)).putObject(any());
  }

  @Test
  void executorGetsShutDown() throws IOException {
    when(objectStoreAccess.getObjectsWithPrefix("version")).thenReturn(emptyList());

    s3Publisher.publish(publishingPath);

    verify(executor, times(1)).shutdown();
  }

  @Test
  void taskExecutionHaltsWhenMaximumFailedOperationsReached() {
    when(objectStoreAccess.getObjectsWithPrefix("version")).thenReturn(emptyList());
    setUpFailureThresholdExceededOnSecondUpload();

    Assertions.assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> s3Publisher.publish(publishingPath));

    // third invocation does not happen
    verify(objectStoreAccess, times(2)).putObject(any());
  }

  @Test
  void threadPoolShutDownWhenMaximumFailedOperationsReached() {
    when(objectStoreAccess.getObjectsWithPrefix("version")).thenReturn(emptyList());
    setUpFailureThresholdExceededOnSecondUpload();

    Assertions.assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> s3Publisher.publish(publishingPath));

    verify(executor, times(1)).shutdown();
  }

  private void setUpFailureThresholdExceededOnSecondUpload() {
    doThrow(ObjectStoreOperationFailedException.class).when(objectStoreAccess).putObject(any());
    doAnswer(ignoredValue -> null)
        .doThrow(ObjectStoreOperationFailedException.class)
        .when(failedObjectStoreOperationsCounter)
        .incrementAndCheckThreshold(any(ObjectStoreOperationFailedException.class));
  }

  @Test
  void interruptedExceptionHandling() throws ExecutionException, InterruptedException {
    var result = mock(Future.class);
    when(result.get()).thenThrow(new InterruptedException());
    doReturn(result).when(executor).submit(any(Runnable.class));
    when(objectStoreAccess.getObjectsWithPrefix("version")).thenReturn(emptyList());

    Assertions.assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> s3Publisher.publish(publishingPath));

    verify(executor, times(1)).shutdown();
  }

  private List<S3Object> otherExisting() {
    return List.of(
        new S3Object("some_old_file.txt", "1fb772815c837b6294d9f163db89e962"),
        new S3Object("other_old_file.txt", "2fb772815c837b6294d9f163db89e962"));
  }

  private List<S3Object> allExistAllSame() {
    return List.of(
        FILE_1,
        FILE_2,
        FILE_3);
  }

  private List<S3Object> twoIdenticalOneOtherOneChange() {
    return List.of(
        new S3Object("newfile.txt", "1fb772815c837b6294d9f163db89e962"), // new
        FILE_1,
        FILE_2,
        new S3Object("file3.txt", "111772815c837b6294d9f163db89e962")); // changed
  }
}
