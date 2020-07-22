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

package app.coronawarn.server.services.distribution.objectstore.publish;

import app.coronawarn.server.services.distribution.objectstore.client.S3Object;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides an overview about which files are currently available on S3.
 */
public class PublishedFileSet {

  /** ta map of S3 objects with the S3 object name as the key component of the map. */
  private Map<String, S3Object> s3Objects;
  private boolean isKeyFilePublishingAllowed;


  /**
   * Creates a new PublishedFileSet for the given S3 objects with the help of the
   * metadata provider. The metadata provider helps to determine whether files
   * have been changed, and are requiring re-upload.
   *
   * @param s3Objects                  the list of s3 objects.
   * @param isKeyFilePublishingAllowed whether the system is currently configured
   *                                   to allow diagnosis key file updates on S3
   */
  public PublishedFileSet(List<S3Object> s3Objects, boolean isKeyFilePublishingAllowed) {
    this.s3Objects = s3Objects.stream()
        .collect(Collectors.toMap(S3Object::getObjectName, s3object -> s3object));
    this.isKeyFilePublishingAllowed = isKeyFilePublishingAllowed;
  }

  /**
   * Checks whether the given file, which is subject for publishing, is already available on the S3. Will return true,
   * when:
   * <ul>
   *   <li>The S3 object key does NOT exist on S3.</li>
   *   <li>The environment variable FORCE_UPDATE_KEYFILES is set to true.</li>
   *   <li>The checksum of the existing S3 object differs to the hash of the given file.</li>
   * </ul>
   *
   * @param file the to-be-published file which should be checked
   * @return {@code true}, if it doesn't exist or differs - {@code false}, if the file has been published already
   */
  public boolean shouldPublish(LocalFile file) {
    S3Object published = s3Objects.get(file.getS3Key());

    if (published == null) {
      return true;
    }

    if (file.isKeyFile()) {
      // #650 - once published key files should not be changed anymore unless explicitly forced
      return isKeyFilePublishingAllowed && contentChanged(file, published);
    }

    return contentChanged(file, published);
  }

  private boolean contentChanged(LocalFile file, S3Object published) {
    return !file.getChecksum().equals(published.getCwaHash());
  }

}
