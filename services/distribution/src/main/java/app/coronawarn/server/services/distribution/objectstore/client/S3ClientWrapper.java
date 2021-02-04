package app.coronawarn.server.services.distribution.objectstore.client;

import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;

import app.coronawarn.server.services.distribution.statistics.exceptions.NotModifiedException;
import app.coronawarn.server.services.distribution.statistics.file.JsonFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Implementation of {@link ObjectStoreClient} that encapsulates an {@link S3Client}.
 */
public class S3ClientWrapper implements ObjectStoreClient {

  private static final Logger logger = LoggerFactory.getLogger(S3ClientWrapper.class);

  private static final int CHUNK_SIZE = 1000;

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

  private JsonFile fromResponse(ResponseInputStream<GetObjectResponse> response) {
    StringBuilder builder = new StringBuilder();
    BufferedReader reader = new BufferedReader(new InputStreamReader(response));
    reader.lines()
        .map(String::strip)
        .forEach(builder::append);
    return new JsonFile(builder.toString(), response.response().eTag());
  }

  @Override
  @Retryable(
      value = SdkException.class,
      maxAttemptsExpression = "${services.distribution.objectstore.retry-attempts}",
      backoff = @Backoff(delayExpression = "${services.distribution.objectstore.retry-backoff}"))
  public JsonFile getSingleObjectContent(String bucket, String key) {
    GetObjectRequest request = GetObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();
    var object = s3Client.getObject(request);
    return fromResponse(object);
  }

  @Override
  @Retryable(
      value = SdkException.class,
      exclude = NotModifiedException.class,
      maxAttemptsExpression = "${services.distribution.objectstore.retry-attempts}",
      backoff = @Backoff(delayExpression = "${services.distribution.objectstore.retry-backoff}"))
  public JsonFile getSingleObjectContent(String bucket, String key, String ifNotETag) throws NotModifiedException {
    try {
      GetObjectRequest request = GetObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .ifNoneMatch(ifNotETag)
          .build();
      var object = s3Client.getObject(request);
      return fromResponse(object);
    } catch (S3Exception ex) {
      if (ex.statusCode() == 304) {
        throw new NotModifiedException(key, ifNotETag);
      } else {
        throw ex;
      }
    }
  }

  @Override
  @Retryable(
      value = SdkException.class,
      maxAttemptsExpression = "${services.distribution.objectstore.retry-attempts}",
      backoff = @Backoff(delayExpression = "${services.distribution.objectstore.retry-backoff}"))
  public List<S3Object> getObjects(String bucket, String prefix) {
    logRetryStatus("object download");
    List<S3Object> allS3Objects = new ArrayList<>();
    String continuationToken = null;

    do {
      ListObjectsV2Request request =
          ListObjectsV2Request.builder().prefix(prefix).bucket(bucket).continuationToken(continuationToken).build();
      ListObjectsV2Response response = s3Client.listObjectsV2(request);
      response.contents().stream()
          .map(s3Object -> buildS3Object(s3Object, bucket))
          .forEach(allS3Objects::add);
      continuationToken = TRUE.equals(response.isTruncated()) ? response.nextContinuationToken() : null;
    } while (continuationToken != null);

    return allS3Objects;
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
    if (headers.containsKey(HeaderKey.CONTENT_TYPE)) {
      requestBuilder.contentType(headers.get(HeaderKey.CONTENT_TYPE));
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
    Collection<DeleteObjectsResponse> response = split(objectNames).stream().map(deleteObjects(bucket))
        .collect(Collectors.toList());
    throwExceptionIfAny(response);
  }

  private void throwExceptionIfAny(Collection<DeleteObjectsResponse> response) {
    response.stream().filter(DeleteObjectsResponse::hasErrors)
        .findFirst()
        .ifPresent(errorResponse -> {
          throw new ObjectStoreOperationFailedException("Failed to remove objects from object store.");
        });
  }

  private Function<List<ObjectIdentifier>, DeleteObjectsResponse> deleteObjects(String bucket) {
    return identifiers -> s3Client.deleteObjects(
        DeleteObjectsRequest.builder().bucket(bucket).delete(Delete.builder().objects(identifiers).build()).build());
  }

  private Collection<List<ObjectIdentifier>> split(List<String> objectNames) {
    AtomicInteger counter = new AtomicInteger();
    return objectNames.stream().map(key -> ObjectIdentifier.builder().key(key).build())
        .collect(toList())
        .stream()
        .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / CHUNK_SIZE))
        .values();
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
