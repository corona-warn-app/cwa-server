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

package app.coronawarn.server.services.distribution.objectstore.client;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Simple Storage Service (aka S3) client to perform bucket and object operations.
 */
public interface ObjectStoreClient {

  /**
   * Downloads the all objects that match the specified prefix from the specified object store bucket.
   *
   * @param bucket The name of the object store bucket.
   * @param prefix The prefix that the names of the returned objects start with.
   * @return A list of objects from the object store that match the specified parameters.
   * @throws ObjectStoreOperationFailedException if the operation could not be performed.
   */
  List<S3Object> getObjects(String bucket, String prefix);

  /**
   * Uploads data from the specified file to an object with the specified name.
   *
   * @param bucket     The name of the object store bucket.
   * @param objectName The name of the target object.
   * @param filePath   The path associated with the file to upload.
   * @param headers    The headers to be used during upload.
   * @throws ObjectStoreOperationFailedException if the operation could not be performed.
   */
  void putObject(String bucket, String objectName, Path filePath, Map<HeaderKey, String> headers);

  /**
   * Removes all the specified objects from the specified object store bucket.
   *
   * @param bucket      The name of the object store bucket.
   * @param objectNames The names of objects to delete.
   * @throws ObjectStoreOperationFailedException if the operation could not be performed.
   */
  void removeObjects(String bucket, List<String> objectNames);

  /**
   * Checks if an object store bucket with the specified name exists.
   *
   * @param bucket The name of the object store bucket.
   * @return True if the bucket exists.
   * @throws ObjectStoreOperationFailedException if the operation could not be performed.
   */
  boolean bucketExists(String bucket);

  /**
   * Provides the supported header keys.
   */
  enum HeaderKey {
    CACHE_CONTROL("Cache-Control"),
    AMZ_ACL("x-amz-acl");

    public final String keyValue;

    HeaderKey(String keyValue) {
      this.keyValue = keyValue;
    }
  }
}
