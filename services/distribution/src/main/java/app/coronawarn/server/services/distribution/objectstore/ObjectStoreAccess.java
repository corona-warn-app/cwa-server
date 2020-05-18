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

import app.coronawarn.server.services.distribution.objectstore.publish.LocalFile;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import io.minio.Result;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.MinioException;
import io.minio.messages.DeleteError;
import io.minio.messages.Item;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>Grants access to the S3 compatible object storage hosted by Telekom in Germany, enabling
 * basic functionality for working with files.</p>
 * <br>
 * Make sure the following properties are available on the env:
 * <ul>
 * <li>cwa.objectstore.endpoint</li>
 * <li>cwa.objectstore.bucket</li>
 * <li>cwa.objectstore.accessKey</li>
 * <li>cwa.objectstore.secretKey</li>
 * <li>cwa.objectstore.port</li>
 * </ul>
 */
@Component
public class ObjectStoreAccess {

  private static final Logger logger = LoggerFactory.getLogger(ObjectStoreAccess.class);

  private static final String DEFAULT_REGION = "eu-west-1";

  private final String bucket;

  private MinioClient client;


  /**
   * Constructs an {@link ObjectStoreAccess} instance for communication with the specified object
   * store endpoint and bucket.
   *
   * @param configurationProperties The config properties
   * @throws IOException When there were problems creating the S3 client
   * @throws GeneralSecurityException When there were problems creating the S3 client
   * @throws MinioException When there were problems creating the S3 client
   */
  @Autowired
  public ObjectStoreAccess(ObjectStoreConfigurationProperties configurationProperties)
      throws IOException, GeneralSecurityException, MinioException {
    this.client = createClient(configurationProperties);

    this.bucket = configurationProperties.getBucket();

    if (!this.client.bucketExists(this.bucket)) {
      throw new IllegalArgumentException("Supplied bucket does not exist " + bucket);
    }
  }

  private MinioClient createClient(ObjectStoreConfigurationProperties configurationProperties)
      throws InvalidPortException, InvalidEndpointException {
    if (isSsl(configurationProperties)) {
      return new MinioClient(
          configurationProperties.getEndpoint(),
          configurationProperties.getPort(),
          configurationProperties.getAccessKey(), configurationProperties.getSecretKey(),
          DEFAULT_REGION,
          true
      );
    } else {
      return new MinioClient(
          configurationProperties.getEndpoint(),
          configurationProperties.getPort(),
          configurationProperties.getAccessKey(), configurationProperties.getSecretKey()
      );
    }
  }

  private boolean isSsl(ObjectStoreConfigurationProperties configurationProperties) {
    return configurationProperties.getEndpoint().startsWith("https://");
  }

  /**
   * Stores the target file on the S3.
   *
   * @param localFile the file to be published
   */
  public void putObject(LocalFile localFile)
      throws IOException, GeneralSecurityException, MinioException {
    String s3Key = localFile.getS3Key();


    var options = new PutObjectOptions(localFile.getFile().toFile().length(), -1);
    options.setHeaders(createMetadataFor(localFile));

    logger.info("... uploading " + s3Key);
    this.client.putObject(bucket, s3Key, localFile.getFile().toString(), options);
  }

  /**
   * Deletes objects in the object store, based on the given prefix (folder structure).
   *
   * @param prefix the prefix, e.g. my/folder/
   */
  public List<DeleteError> deleteObjectsWithPrefix(String prefix)
      throws MinioException, GeneralSecurityException, IOException {
    List<String> toDelete = getObjectsWithPrefix(prefix)
        .stream()
        .map(S3Object::getObjectName)
        .collect(Collectors.toList());

    logger.info("Deleting " + toDelete.size() + " entries with prefix " + prefix);
    var deletionResponse = this.client.removeObjects(bucket, toDelete);

    List<DeleteError> errors = new ArrayList<>();
    for (Result<DeleteError> deleteErrorResult : deletionResponse) {
      errors.add(deleteErrorResult.get());
    }

    logger.info("Deletion result: " + errors.size());

    return errors;
  }

  /**
   * Fetches the list of objects in the store with the given prefix.
   *
   * @param prefix the prefix, e.g. my/folder/
   * @return the list of objects
   */
  public List<S3Object> getObjectsWithPrefix(String prefix)
      throws IOException, GeneralSecurityException, MinioException {
    var objects = this.client.listObjects(bucket, prefix, true, true, false);

    var list = new ArrayList<S3Object>();
    for (Result<Item> item : objects) {
      list.add(S3Object.of(item.get()));
    }

    return list;
  }

  private Map<String, String> createMetadataFor(LocalFile file) {
    return Map.of("cwa.hash", file.getHash());
  }
}
