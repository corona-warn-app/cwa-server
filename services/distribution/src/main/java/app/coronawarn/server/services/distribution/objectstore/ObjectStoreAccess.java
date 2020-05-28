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

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.publish.LocalFile;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import io.minio.Result;
import io.minio.errors.MinioException;
import io.minio.messages.DeleteError;
import io.minio.messages.Item;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
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

  private final MinioClient client;

  /**
   * Constructs an {@link ObjectStoreAccess} instance for communication with the specified object store endpoint and
   * bucket.
   *
   * @param distributionServiceConfig The config properties
   * @param minioClient               The client used for interaction with the object store
   * @throws IOException              When there were problems creating the S3 client
   * @throws GeneralSecurityException When there were problems creating the S3 client
   * @throws MinioException           When there were problems creating the S3 client
   */
  ObjectStoreAccess(DistributionServiceConfig distributionServiceConfig, MinioClient minioClient)
      throws IOException, GeneralSecurityException, MinioException {
    this.client = minioClient;
    this.bucket = distributionServiceConfig.getObjectStore().getBucket();
    this.isSetPublicReadAclOnPutObject = distributionServiceConfig.getObjectStore().isSetPublicReadAclOnPutObject();

    if (!this.client.bucketExists(this.bucket)) {
      throw new IllegalArgumentException("Supplied bucket does not exist " + bucket);
    }
  }

  /**
   * Stores the target file on the S3 and sets cache control headers according to the default maximum age value.
   *
   * @param localFile The file to be published.
   */
  public void putObject(LocalFile localFile) throws IOException, GeneralSecurityException, MinioException {
    putObject(localFile, DEFAULT_MAX_CACHE_AGE);
  }

  /**
   * Stores the target file on the S3 and sets cache control headers according to the specified maximum age value.
   *
   * @param localFile The file to be published.
   * @param maxAge    A cache control parameter that specifies the maximum amount of time in seconds that a resource can
   *                  be considered "fresh" when held in a cache.
   */
  public void putObject(LocalFile localFile, int maxAge) throws IOException, GeneralSecurityException, MinioException {
    String s3Key = localFile.getS3Key();
    PutObjectOptions options = createOptionsFor(localFile, maxAge);

    logger.info("... uploading {}", s3Key);
    this.client.putObject(bucket, s3Key, localFile.getFile().toString(), options);
  }

  /**
   * Deletes objects in the object store, based on the given prefix (folder structure).
   *
   * @param prefix the prefix, e.g. my/folder/
   */
  public void deleteObjectsWithPrefix(String prefix)
      throws MinioException, GeneralSecurityException, IOException {
    List<String> toDelete = getObjectsWithPrefix(prefix)
        .stream()
        .map(S3Object::getObjectName)
        .collect(Collectors.toList());

    logger.info("Deleting {} entries with prefix {}", toDelete.size(), prefix);
    var deletionResponse = this.client.removeObjects(bucket, toDelete);

    List<DeleteError> errors = new ArrayList<>();
    for (Result<DeleteError> deleteErrorResult : deletionResponse) {
      errors.add(deleteErrorResult.get());
    }

    if (!errors.isEmpty()) {
      throw new MinioException("Can't delete files, number of errors: " + errors.size());
    }
  }

  /**
   * Fetches the list of objects in the store with the given prefix.
   *
   * @param prefix the prefix, e.g. my/folder/
   * @return the list of objects
   */
  public List<S3Object> getObjectsWithPrefix(String prefix)
      throws IOException, GeneralSecurityException, MinioException {
    var objects = this.client.listObjects(bucket, prefix, true);

    var list = new ArrayList<S3Object>();
    for (Result<Item> item : objects) {
      list.add(S3Object.of(item.get()));
    }

    return list;
  }

  private PutObjectOptions createOptionsFor(LocalFile file, int maxAge) {
    var options = new PutObjectOptions(file.getFile().toFile().length(), -1);

    Map<String, String> headers = new HashMap<>(Map.of("cache-control", "public,max-age=" + maxAge));
    if (this.isSetPublicReadAclOnPutObject) {
      headers.put("x-amz-acl", "public-read");
    }
    options.setHeaders(headers);

    return options;
  }

}
