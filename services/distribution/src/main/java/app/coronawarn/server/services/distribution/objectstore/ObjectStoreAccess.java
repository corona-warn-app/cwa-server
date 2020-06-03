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
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreClient;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreClient.HeaderKey;
import app.coronawarn.server.services.distribution.objectstore.client.S3Object;
import app.coronawarn.server.services.distribution.objectstore.publish.LocalFile;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * <p>Grants access to the S3 compatible object storage hosted by Telekom in Germany, enabling
 * basic functionality for working with files.</p>
 * <br>
 * Make sure the following properties are available on the env:
 * <ul>
 * <li>services.distribution.objectstore.endpoint</li>
 * <li>services.distribution.objectstore.bucket</li>
 * <li>services.distribution.objectstore.accessKey</li>
 * <li>services.distribution.objectstore.secretKey</li>
 * <li>services.distribution.objectstore.port</li>
 * </ul>
 */
@Component
public class ObjectStoreAccess {

  private static final Logger logger = LoggerFactory.getLogger(ObjectStoreAccess.class);

  /**
   * Specifies the default maximum amount of time in seconds that a published resource can be considered "fresh" when
   * held in a cache.
   */
  public static final int DEFAULT_MAX_CACHE_AGE = 300;

  private final boolean isSetPublicReadAclOnPutObject;

  private final String bucket;

  private final ObjectStoreClient client;

  /**
   * Constructs an {@link ObjectStoreAccess} instance for communication with the specified object store endpoint and
   * bucket.
   *
   * @param distributionServiceConfig The config properties
   * @param objectStoreClient         The client used for interaction with the object store
   */
  ObjectStoreAccess(DistributionServiceConfig distributionServiceConfig, ObjectStoreClient objectStoreClient) {
    this.client = objectStoreClient;
    this.bucket = distributionServiceConfig.getObjectStore().getBucket();
    this.isSetPublicReadAclOnPutObject = distributionServiceConfig.getObjectStore().isSetPublicReadAclOnPutObject();

    if (!this.client.bucketExists(this.bucket)) {
      throw new IllegalArgumentException("No bucket with the specified name exists: " + bucket);
    }
  }

  /**
   * Stores the target file on the S3 and sets cache control headers according to the default maximum age value.
   *
   * @param localFile The file to be published.
   */
  public void putObject(LocalFile localFile) {
    putObject(localFile, DEFAULT_MAX_CACHE_AGE);
  }

  /**
   * Stores the target file on the S3 and sets cache control headers according to the specified maximum age value.
   *
   * @param localFile The file to be published.
   * @param maxAge    A cache control parameter that specifies the maximum amount of time in seconds that a resource can
   *                  be considered "fresh" when held in a cache.
   */
  public void putObject(LocalFile localFile, int maxAge) {
    String s3Key = localFile.getS3Key();
    Map<HeaderKey, String> headers = createHeaders(maxAge);

    logger.info("... uploading {}", s3Key);
    try {
      this.client.putObject(bucket, s3Key, localFile.getFile(), headers);
    } catch (Exception e) {
      logger.error("Cannot upload file! ", e);
    }
  }

  /**
   * Deletes objects in the object store, based on the given prefix (folder structure).
   *
   * @param prefix the prefix, e.g. my/folder/
   */
  public void deleteObjectsWithPrefix(String prefix) {
    List<String> toDelete = getObjectsWithPrefix(prefix)
        .stream()
        .map(S3Object::getObjectName)
        .collect(Collectors.toList());

    logger.info("Deleting {} entries with prefix {}", toDelete.size(), prefix);
    this.client.removeObjects(bucket, toDelete);
  }

  /**
   * Fetches the list of objects in the store with the given prefix.
   *
   * @param prefix the prefix, e.g. my/folder/
   * @return the list of objects
   */
  public List<S3Object> getObjectsWithPrefix(String prefix) {
    return client.getObjects(bucket, prefix);
  }

  private Map<HeaderKey, String> createHeaders(int maxAge) {
    EnumMap<HeaderKey, String> headers = new EnumMap<>(Map.of(HeaderKey.CACHE_CONTROL, "public,max-age=" + maxAge));
    if (this.isSetPublicReadAclOnPutObject) {
      headers.put(HeaderKey.AMZ_ACL, "public-read");
    }
    return headers;
  }
}
