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

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreOperationFailedException;
import app.coronawarn.server.services.distribution.objectstore.publish.LocalFile;
import app.coronawarn.server.services.distribution.objectstore.publish.PublishFileSet;
import app.coronawarn.server.services.distribution.objectstore.publish.PublishedFileSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * Publishes a folder on the disk to S3 while keeping the folder and file structure.<br> Moreover, does the following:
 * <br>
 * <ul>
 *   <li>Publishes index files on a different route, removing the trailing "/index" part.</li>
 *   <li>Adds meta information to the uploaded files, e.g. the sha256 hash value.</li>
 *   <li>Only performs the upload for files, which do not yet exist on the object store, and
 *   checks whether the existing files hash differ from the to-be-uploaded files hash. Only if the
 *   hash differs, the file will ultimately be uploaded</li>
 *   <li>Currently not implemented: Set cache control headers</li>
 *   <li>Currently not implemented: Supports multi threaded upload of files.</li>
 * </ul>
 */
@Component
public class S3Publisher {

  private static final Logger logger = LoggerFactory.getLogger(S3Publisher.class);

  private final ObjectStoreAccess objectStoreAccess;
  private final FailedObjectStoreOperationsCounter failedOperationsCounter;
  private final ThreadPoolTaskExecutor executor;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Creates an {@link S3Publisher} instance that attempts to publish the files at the specified location to an object
   * store. Object store operations are performed through the specified {@link ObjectStoreAccess} instance.
   *
   * @param objectStoreAccess         The {@link ObjectStoreAccess} used to communicate with the object store.
   * @param failedOperationsCounter   The {@link FailedObjectStoreOperationsCounter} that is used to monitor the number
   *                                  of failed operations.
   * @param executor                  The executor that manages the upload task submission.
   * @param distributionServiceConfig The {@link DistributionServiceConfig} used for distribution service
   *                                  configuration.
   */
  public S3Publisher(ObjectStoreAccess objectStoreAccess, FailedObjectStoreOperationsCounter failedOperationsCounter,
      ThreadPoolTaskExecutor executor, DistributionServiceConfig distributionServiceConfig) {
    this.objectStoreAccess = objectStoreAccess;
    this.failedOperationsCounter = failedOperationsCounter;
    this.executor = executor;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  /**
   * Synchronizes the files to S3. Current strategy is to never update diagnosis key archive files already
   * published on S3, even if the retention and shifting policies cause a diff between subsequent distribution runs.
   * Thus, by default distribution will only add new key files, but still modify indexes. This behaviour can however
   * be controlled through the configuration parameter <code>DistributionServiceConfig.forceUpdateKeyFiles</code>
   *
   * @param root The path of the directory that shall be published.
   * @see Github issue #650
   * @throws IOException in case there were problems reading files from the disk.
   */
  public void publish(Path root) throws IOException {
    List<LocalFile> toPublish = new PublishFileSet(root).getFiles();

    PublishedFileSet published = new PublishedFileSet(
        objectStoreAccess.getObjectsWithPrefix(distributionServiceConfig.getApi().getVersionPath()),
        distributionServiceConfig.getObjectStore().getForceUpdateKeyfiles());
    List<LocalFile> diff = toPublish
        .stream()
        .filter(published::shouldPublish)
        .collect(Collectors.toList());

    logger.info("Beginning upload of {} files... ", diff.size());
    try {
      diff.stream()
          .map(file -> executor.submit(() -> objectStoreAccess.putObject(file)))
          .forEach(this::awaitThread);
    } finally {
      executor.shutdown();
    }
    logger.info("Upload completed.");
  }

  private void awaitThread(Future<?> result) {
    try {
      result.get();
    } catch (ExecutionException e) {
      failedOperationsCounter.incrementAndCheckThreshold(new ObjectStoreOperationFailedException(e.getMessage(), e));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ObjectStoreOperationFailedException(e.getMessage(), e);
    }
  }
}
