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

import static java.util.stream.Collectors.toList;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Implementation of {@link ObjectStoreClient} that encapsulates an {@link S3Client}.
 */
public class S3ClientWrapper implements ObjectStoreClient {

  private static final Logger logger = LoggerFactory.getLogger(S3ClientWrapper.class);

  private final S3Client s3Client;

  public S3ClientWrapper(S3Client s3Client) {
    this.s3Client = s3Client;
  }

  @Override
  public boolean bucketExists(String bucketName) {
    try {
      // using S3Client.listObjectsV2 instead of S3Client.listBuckets/headBucket in order to limit required permissions
      s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).maxKeys(1).build());
      return true;
    } catch (NoSuchBucketException e) {
      return false;
    } catch (SdkException e) {
      throw new ObjectStoreOperationFailedException("Failed to determine if bucket exists.", e);
    }
  }

  @Override
  @Retryable(
      value = SdkException.class,
      maxAttemptsExpression = "${services.distribution.objectstore.retry-attempts}",
      backoff = @Backoff(delayExpression = "${services.distribution.objectstore.retry-backoff}"))
  public List<S3Object> getObjects(String bucket, String prefix) {
    logRetryStatus("object download");

    ListObjectsV2Response response =
        s3Client.listObjectsV2(ListObjectsV2Request.builder().prefix(prefix).bucket(bucket).build());

    return response.contents().stream()
        .map(s3Object -> buildS3Object(s3Object, bucket))
        .collect(toList());
  }

  @Recover
  public List<S3Object> skipReadOperation(Throwable cause) {
    throw new ObjectStoreOperationFailedException("Failed to get objects from object store", cause);
  }

  @Override
  @Retryable(
      value = SdkException.class,
      maxAttemptsExpression = "${services.distribution.objectstore.retry-attempts}",
      backoff = @Backoff(delayExpression = "${services.distribution.objectstore.retry-backoff}"))
  public void putObject(String bucket, String objectName, Path filePath, Map<HeaderKey, String> headers) {
    logRetryStatus("object upload");
    var requestBuilder = PutObjectRequest.builder().bucket(bucket).key(objectName);
    if (headers.containsKey(HeaderKey.AMZ_ACL)) {
      requestBuilder.acl(headers.get(HeaderKey.AMZ_ACL));
    }
    if (headers.containsKey(HeaderKey.CACHE_CONTROL)) {
      requestBuilder.cacheControl(headers.get(HeaderKey.CACHE_CONTROL));
    }
    if (headers.containsKey(HeaderKey.CWA_HASH)) {
      requestBuilder.metadata(Map.of(HeaderKey.CWA_HASH.withMetaPrefix(), headers.get(HeaderKey.CWA_HASH)));
    }

    RequestBody bodyFile = RequestBody.fromFile(filePath);
    s3Client.putObject(requestBuilder.build(), bodyFile);
  }

  @Override
  @Retryable(value = {SdkException.class, ObjectStoreOperationFailedException.class},
      maxAttemptsExpression = "${services.distribution.objectstore.retry-attempts}",
      backoff = @Backoff(delayExpression = "${services.distribution.objectstore.retry-backoff}"))
  public void removeObjects(String bucket, List<String> objectNames) {
    if (objectNames.isEmpty()) {
      return;
    }
    logRetryStatus("object deletion");

    Collection<ObjectIdentifier> identifiers = objectNames.stream()
        .map(key -> ObjectIdentifier.builder().key(key).build()).collect(toList());

    DeleteObjectsResponse response = s3Client.deleteObjects(
        DeleteObjectsRequest.builder()
            .bucket(bucket)
            .delete(Delete.builder().objects(identifiers).build()).build());

    if (response.hasErrors()) {
      throw new ObjectStoreOperationFailedException("Failed to remove objects from object store.");
    }
  }

  @Recover
  private void skipModifyingOperation(Throwable cause) {
    throw new ObjectStoreOperationFailedException("Failed to modify objects on object store.", cause);
  }

  /**
   * Fetches the CWA Hash for the given S3Object. Unfortunately, this is necessary for the AWS SDK, as it does not
   * support fetching metadata within the {@link ListObjectsV2Request}.<br> MinIO actually does support this, so when
   * they release 7.0.3, we can remove this code here.
   *
   * @param s3Object the S3Object to fetch the CWA hash for
   * @param bucket   the target bucket
   * @return the CWA hash as a String, or null, if there is no CWA hash available on that object.
   */
  private String fetchCwaHash(software.amazon.awssdk.services.s3.model.S3Object s3Object, String bucket) {
    var result = this.s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(s3Object.key()).build());
    return result.metadata().get(HeaderKey.CWA_HASH.keyValue);
  }

  private S3Object buildS3Object(software.amazon.awssdk.services.s3.model.S3Object s3Object, String bucket) {
    String cwaHash = fetchCwaHash(s3Object, bucket);
    return new S3Object(s3Object.key(), cwaHash);
  }

  private void logRetryStatus(String action) {
    int retryCount = RetrySynchronizationManager.getContext().getRetryCount();
    if (retryCount > 0) {
      logger.warn("Retrying {} after {} failed attempt(s).", action, retryCount);
    }
  }
}
