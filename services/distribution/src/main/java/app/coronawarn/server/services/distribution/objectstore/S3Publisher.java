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

import app.coronawarn.server.services.distribution.assembly.component.CwaApiStructureProvider;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreOperationFailedException;
import app.coronawarn.server.services.distribution.objectstore.publish.LocalFile;
import app.coronawarn.server.services.distribution.objectstore.publish.PublishFileSet;
import app.coronawarn.server.services.distribution.objectstore.publish.PublishedFileSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publishes a folder on the disk to S3 while keeping the folder and file structure.<br>
 * Moreover, does the following:
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
public class S3Publisher {

  private static final Logger logger = LoggerFactory.getLogger(S3Publisher.class);

  /** The default CWA root folder, which contains all CWA related files. */
  private static final String CWA_S3_ROOT = CwaApiStructureProvider.VERSION_DIRECTORY;

  /** root folder for the upload on the local disk. */
  private final Path root;

  /** access to the object store. */
  private final ObjectStoreAccess objectStoreAccess;

  private final FailedObjectStoreOperationsCounter failedOperationsCounter;

  public S3Publisher(Path root, ObjectStoreAccess objectStoreAccess,
      FailedObjectStoreOperationsCounter failedOperationsCounter) {
    this.root = root;
    this.objectStoreAccess = objectStoreAccess;
    this.failedOperationsCounter = failedOperationsCounter;
  }

  /**
   * Synchronizes the files to S3.
   *
   * @throws IOException in case there were problems reading files from the disk.
   */
  public void publish() throws IOException {
    PublishedFileSet published;
    List<LocalFile> toPublish = new PublishFileSet(root).getFiles();
    List<LocalFile> diff;

    try {
      published = new PublishedFileSet(objectStoreAccess.getObjectsWithPrefix(CWA_S3_ROOT));
      diff = toPublish
          .stream()
          .filter(published::isNotYetPublished)
          .collect(Collectors.toList());
    } catch (ObjectStoreOperationFailedException e) {
      failedOperationsCounter.incrementAndCheckThreshold(e);
      // failed to retrieve existing files; publish everything
      diff = toPublish;
    }

    logger.info("Beginning upload... ");
    for (LocalFile file : diff) {
      try {
        this.objectStoreAccess.putObject(file);
      } catch (ObjectStoreOperationFailedException e) {
        failedOperationsCounter.incrementAndCheckThreshold(e);
      }
    }
    logger.info("Upload completed.");
  }
}
