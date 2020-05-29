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

package app.coronawarn.server.services.distribution.objectstore.client;

import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Item;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link ObjectStoreClient} that encapsulates a {@link MinioClient}.
 */
public class MinioClientWrapper implements ObjectStoreClient {

  private final MinioClient minioClient;

  public MinioClientWrapper(MinioClient minioClient) {
    this.minioClient = minioClient;
  }

  @Override
  public List<S3Object> getObjects(String bucket, String prefix) {
    var objects = this.minioClient.listObjects(bucket, prefix, true);

    var list = new ArrayList<S3Object>();
    for (Result<Item> item : objects) {
      try {
        list.add(S3Object.of(item.get()));
      } catch (ErrorResponseException | NoSuchAlgorithmException | InternalException | IOException | InvalidKeyException
          | InvalidResponseException | InvalidBucketNameException | InsufficientDataException | XmlParserException e) {
        throw new ObjectStoreOperationFailedException("Failed to download objects from object store.", e);
      }
    }
    return list;
  }

  @Override
  public void putObject(String bucket, String objectName, Path filePath, Map<String, String> headers) {
    try {
      var options = new PutObjectOptions(Files.size(filePath), -1);
      options.setHeaders(headers);
      minioClient.putObject(bucket, objectName, filePath.toString(), options);
    } catch (ErrorResponseException | NoSuchAlgorithmException | InternalException | IOException | InvalidKeyException
        | InvalidResponseException | InvalidBucketNameException | InsufficientDataException | XmlParserException e) {
      throw new ObjectStoreOperationFailedException("Failed to upload object to object store.", e);
    }
  }

  @Override
  public void removeObjects(String bucket, List<String> objectNames) {
    if (minioClient.removeObjects(bucket, objectNames).iterator().hasNext()) {
      throw new ObjectStoreOperationFailedException("Failed to remove objects from object store");
    }
  }

  @Override
  public boolean bucketExists(String bucket) {
    try {
      return minioClient.bucketExists(bucket);
    } catch (ErrorResponseException | NoSuchAlgorithmException | InternalException | IOException | InvalidKeyException
        | InvalidResponseException | InvalidBucketNameException | InsufficientDataException | XmlParserException e) {
      throw new ObjectStoreOperationFailedException("Failed to check if object store bucket exists.", e);
    }
  }
}
