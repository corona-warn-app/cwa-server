

package app.coronawarn.server.services.distribution.objectstore.client;

import static java.util.Collections.EMPTY_MAP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.util.Maps.newHashMap;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreClient.HeaderKey;
import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import app.coronawarn.server.services.distribution.statistics.exceptions.NotModifiedException;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.ExhaustedRetryException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Error;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.utils.builder.SdkBuilder;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@EnableConfigurationProperties(value = DistributionServiceConfig.class)
class S3ClientWrapperTest {

  private static final String VALID_BUCKET_NAME = "myBucket";
  private static final String VALID_PREFIX = "prefix";
  private static final String VALID_NAME = "object key";
  private static final Path VALID_PATH = Path.of("");

  @Value("${services.distribution.objectstore.retry-attempts}")
  private int configuredNumberOfRetries;

  @MockBean
  private S3Client s3Client;

  @Autowired
  private ObjectStoreClient s3ClientWrapper;

  @Configuration
  @EnableRetry
  public static class RetryS3ClientConfig {

    @Bean(name = "publish-s3")
    @ConditionalOnMissingBean
    public ObjectStoreClient createObjectStoreClient(S3Client s3Client) {
      return new S3ClientWrapper(s3Client);
    }
  }

  @BeforeEach
  public void setUpMocks() {
    reset(s3Client);
  }

  @Test
  void testBucketExistsIfBucketExists() {
    when(s3Client.listObjectsV2((any(ListObjectsV2Request.class)))).thenReturn(ListObjectsV2Response.builder().build());
    assertThat(s3ClientWrapper.bucketExists(VALID_BUCKET_NAME)).isTrue();
  }

  @Test
  void testBucketExistsIfBucketDoesNotExist() {
    when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenThrow(NoSuchBucketException.class);
    assertThat(s3ClientWrapper.bucketExists(VALID_BUCKET_NAME)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(classes = {S3Exception.class, SdkClientException.class, SdkException.class})
  void bucketExistsThrowsObjectStoreOperationFailedExceptionIfClientThrows(Class<Exception> cause) {
    when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenThrow(cause);
    assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> s3ClientWrapper.bucketExists(VALID_BUCKET_NAME));
  }

  @Test
  void testGetObjectsSendsCorrectRequest() {
    when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(ListObjectsV2Response.builder().build());

    s3ClientWrapper.getObjects(VALID_BUCKET_NAME, VALID_PREFIX);

    ListObjectsV2Request expRequest = ListObjectsV2Request.builder()
        .prefix(VALID_PREFIX).bucket(VALID_BUCKET_NAME).build();
    verify(s3Client, atLeastOnce()).listObjectsV2(eq(expRequest));
  }

  @ParameterizedTest
  @MethodSource("createGetObjectsResults")
  void testGetObjects(List<S3Object> expResult) {
    ListObjectsV2Response actResponse = buildListObjectsResponse(expResult);
    when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(actResponse);
    when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder().build());

    List<S3Object> actResult = s3ClientWrapper.getObjects(VALID_BUCKET_NAME, VALID_PREFIX);

    assertThat(actResult).isEqualTo(expResult);
  }

  @Test
  void testContinuationToken() {
    var continuationToken = "1ueGcxLPRx1Tr/XYExHnhbYLgveDs2J/wm36Hy4vbOwM=<";

    when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
        .thenReturn(ListObjectsV2Response.builder().isTruncated(true).nextContinuationToken(continuationToken).build(),
            ListObjectsV2Response.builder().isTruncated(false).build());

    s3ClientWrapper.getObjects(VALID_BUCKET_NAME, VALID_PREFIX);

    ListObjectsV2Request continuationRequest = ListObjectsV2Request.builder()
        .prefix(VALID_PREFIX).bucket(VALID_BUCKET_NAME).continuationToken(continuationToken).build();
    ListObjectsV2Request noContinuationRequest = ListObjectsV2Request.builder()
        .prefix(VALID_PREFIX).bucket(VALID_BUCKET_NAME).build();

    verify(s3Client, times(1)).listObjectsV2(eq(continuationRequest));
    verify(s3Client, times(1)).listObjectsV2(eq(noContinuationRequest));
  }

  private static Stream<Arguments> createGetObjectsResults() {
    return Stream.of(
        Lists.emptyList(),
        Lists.list(new S3Object("objName")),
        Lists.list(new S3Object("objName1"), new S3Object("objName2"))
    ).map(Arguments::of);
  }

  private ListObjectsV2Response buildListObjectsResponse(List<S3Object> s3Objects) {
    var responseObjects = s3Objects.stream().map(
        s3Object -> software.amazon.awssdk.services.s3.model.S3Object.builder()
            .key(s3Object.getObjectName()))
        .map(SdkBuilder::build).collect(Collectors.toList());
    return ListObjectsV2Response.builder().contents(responseObjects).build();
  }

  @ParameterizedTest
  @ValueSource(classes = {NoSuchBucketException.class, S3Exception.class, SdkClientException.class, SdkException.class})
  void getObjectsThrowsObjectStoreOperationFailedExceptionIfClientThrows(Class<Exception> cause) {
    when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenThrow(cause);
    assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> s3ClientWrapper.getObjects(VALID_BUCKET_NAME, VALID_PREFIX));
  }

  @ParameterizedTest
  @ValueSource(classes = {NoSuchBucketException.class, S3Exception.class, SdkClientException.class, SdkException.class})
  void shouldRetryGettingObjectsAndThenThrow(Class<Exception> cause) {
    when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenThrow(cause);
    assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> s3ClientWrapper.getObjects(VALID_BUCKET_NAME, VALID_PREFIX));

    verify(s3Client, times(configuredNumberOfRetries)).listObjectsV2(any(ListObjectsV2Request.class));
  }

  @Test
  void testPutObjectForNoHeaders() {
    s3ClientWrapper.putObject(VALID_BUCKET_NAME, VALID_NAME, VALID_PATH, EMPTY_MAP);

    PutObjectRequest expRequest = PutObjectRequest.builder().bucket(VALID_BUCKET_NAME).key(VALID_NAME).build();
    verify(s3Client, atLeastOnce()).putObject(eq(expRequest), any(RequestBody.class));
  }

  @Test
  void testPutObjectForContentTypeHeader() {
    String contentType = "foo-content-type";
    s3ClientWrapper.putObject(VALID_BUCKET_NAME, VALID_NAME, Path.of(""),
        newHashMap(HeaderKey.CONTENT_TYPE, contentType));

    PutObjectRequest expRequest =
        PutObjectRequest.builder().bucket(VALID_BUCKET_NAME).key(VALID_NAME).contentType(contentType).build();
    verify(s3Client, atLeastOnce()).putObject(eq(expRequest), any(RequestBody.class));
  }

  @Test
  void testPutObjectForCacheControlHeader() {
    var expCacheControl = "foo-cache-control";
    s3ClientWrapper
        .putObject(VALID_BUCKET_NAME, VALID_NAME, VALID_PATH, newHashMap(HeaderKey.CACHE_CONTROL, expCacheControl));

    PutObjectRequest expRequest =
        PutObjectRequest.builder().bucket(VALID_BUCKET_NAME).key(VALID_NAME).cacheControl(expCacheControl).build();
    verify(s3Client, atLeastOnce()).putObject(eq(expRequest), any(RequestBody.class));
  }

  @Test
  void testPutObjectForAmzAclHeader() {
    String expAcl = "foo-acl";
    s3ClientWrapper.putObject(VALID_BUCKET_NAME, VALID_NAME, VALID_PATH, newHashMap(HeaderKey.AMZ_ACL, expAcl));

    PutObjectRequest expRequest =
        PutObjectRequest.builder().bucket(VALID_BUCKET_NAME).key(VALID_NAME).acl(expAcl).build();
    verify(s3Client, atLeastOnce()).putObject(eq(expRequest), any(RequestBody.class));
  }

  @ParameterizedTest
  @ValueSource(classes = {NoSuchBucketException.class, S3Exception.class, SdkClientException.class, SdkException.class})
  void putObjectsThrowsObjectStoreOperationFailedExceptionIfClientThrows(Class<Exception> cause) {
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenThrow(cause);
    assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> s3ClientWrapper.putObject(VALID_BUCKET_NAME, VALID_PREFIX, VALID_PATH, EMPTY_MAP));
  }

  @ParameterizedTest
  @ValueSource(classes = {NoSuchBucketException.class, S3Exception.class, SdkClientException.class, SdkException.class})
  void shouldRetryUploadingObjectAndThenThrow(Class<Exception> cause) {
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenThrow(cause);
    assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> s3ClientWrapper.putObject(VALID_BUCKET_NAME, VALID_PREFIX, VALID_PATH, EMPTY_MAP));

    verify(s3Client, times(configuredNumberOfRetries)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  void testRemoveObjects() {
    when(s3Client.deleteObjects(any(DeleteObjectsRequest.class))).thenReturn(DeleteObjectsResponse.builder().build());
    List<String> expObjectNames = List.of("obj1", "obj2");

    s3ClientWrapper.removeObjects(VALID_BUCKET_NAME, expObjectNames);

    DeleteObjectsRequest expRequest = DeleteObjectsRequest.builder()
        .bucket(VALID_BUCKET_NAME).delete(buildDeleteObject(expObjectNames)).build();
    verify(s3Client, atLeastOnce()).deleteObjects(eq(expRequest));
  }

  private Delete buildDeleteObject(List<String> objectNames) {
    return Delete.builder().objects(objectNames.stream().map(
        key -> ObjectIdentifier.builder().key(key).build()).collect(Collectors.toList())).build();
  }

  @Test
  void removeObjectsThrowsOnDeletionErrors() {
    DeleteObjectsResponse actResponse = DeleteObjectsResponse.builder().errors(S3Error.builder().build()).build();
    when(s3Client.deleteObjects(any(DeleteObjectsRequest.class))).thenReturn(actResponse);
    List<String> targetObjects = List.of(VALID_NAME);

    assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> s3ClientWrapper.removeObjects(VALID_BUCKET_NAME, targetObjects));
  }

  @ParameterizedTest
  @ValueSource(classes = {NoSuchBucketException.class, S3Exception.class, SdkClientException.class, SdkException.class})
  void removeObjectsThrowsObjectStoreOperationFailedExceptionIfClientThrows(Class<Exception> cause) {
    when(s3Client.deleteObjects(any(DeleteObjectsRequest.class))).thenThrow(cause);
    List<String> targetObjects = List.of(VALID_NAME);

    assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> s3ClientWrapper.removeObjects(VALID_BUCKET_NAME, targetObjects));
  }

  @ParameterizedTest
  @ValueSource(classes = {NoSuchBucketException.class, S3Exception.class, SdkClientException.class, SdkException.class})
  void shouldRetryRemovingObjectAndThenThrow(Class<Exception> cause) {
    when(s3Client.deleteObjects(any(DeleteObjectsRequest.class))).thenThrow(cause);
    List<String> targetObjects = List.of(VALID_NAME);

    assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> s3ClientWrapper.removeObjects(VALID_BUCKET_NAME, targetObjects));

    verify(s3Client, times(configuredNumberOfRetries)).deleteObjects(any(DeleteObjectsRequest.class));
  }

  private <T> ResponseInputStream<T> makeGetObjectStreamedResponse(T response, String body) {
    var stream = new ByteArrayInputStream(body.getBytes());
    return new ResponseInputStream<T>(response, AbortableInputStream.create(stream));
  }

  @Test
  void getSingleObjectContent() {
    GetObjectResponse response = GetObjectResponse.builder()
        .eTag("abc")
        .build();
    var httpResponse = makeGetObjectStreamedResponse(response, "[]");
    when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(httpResponse);
    var jsonFile = s3ClientWrapper.getSingleObjectContent("example", "key1");
    assertThat(jsonFile.getETag()).isEqualTo("abc");
    assertThat(jsonFile.getContent()).isEqualTo("[]");
  }

  @Test
  void jsonFileEtagIsNullWhenNotReceived() {
    GetObjectResponse response = GetObjectResponse.builder()
        .build();
    var httpResponse = makeGetObjectStreamedResponse(response, "[]");
    when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(httpResponse);
    var jsonFile = s3ClientWrapper.getSingleObjectContent("example", "key1");
    assertThat(jsonFile.getETag()).isNull();
    assertThat(jsonFile.getContent()).isEqualTo("[]");
  }

  @ParameterizedTest
  @ValueSource(classes = {NoSuchBucketException.class, S3Exception.class, SdkClientException.class, SdkException.class})
  void shouldThrowSameExceptionAsAwsClient(Class<Exception> cause) {
    when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(cause);
    assertThatExceptionOfType(ExhaustedRetryException.class)
        .isThrownBy(() -> s3ClientWrapper.getSingleObjectContent("", ""))
        .withCauseExactlyInstanceOf(cause);
  }

  @Test
  void shouldReturnNewObjectIfEtagDoesntMatch() throws NotModifiedException {
    GetObjectResponse response = GetObjectResponse.builder()
        .eTag("abc")
        .build();
    var httpResponse = makeGetObjectStreamedResponse(response, "[]");
    when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(httpResponse);
    var jsonFile = s3ClientWrapper.getSingleObjectContent("example", "key1", "cba");
    assertThat(jsonFile.getETag()).isEqualTo("abc");
    assertThat(jsonFile.getContent()).isEqualTo("[]");
  }

  @Test
  void shouldThrowNotModifiedExceptionWhenStatusCodeIs304() {
    when(s3Client.getObject(any(GetObjectRequest.class)))
        .thenThrow(S3Exception.builder().statusCode(304).build());
    assertThatExceptionOfType(ExhaustedRetryException.class)
        .isThrownBy(() -> s3ClientWrapper.getSingleObjectContent("abc", "key1", "abc"))
        .withCauseExactlyInstanceOf(NotModifiedException.class);
  }

  @ParameterizedTest
  @ValueSource(classes = {NoSuchBucketException.class, S3Exception.class, SdkClientException.class, SdkException.class})
  void shouldThrowSameExceptionAsAwsClientIfNot304(Class<Exception> cause) {
    when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(cause);
    assertThatExceptionOfType(ExhaustedRetryException.class)
        .isThrownBy(() -> s3ClientWrapper.getSingleObjectContent("", "", "etag"))
        .withCauseExactlyInstanceOf(cause);
  }
}
