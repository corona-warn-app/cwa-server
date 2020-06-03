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

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.util.Maps.newHashMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreClient.HeaderKey;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Error;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.utils.builder.SdkBuilder;

@ExtendWith(SpringExtension.class)
class S3ClientWrapperTest {

  private static final String VALID_BUCKET_NAME = "myBucket";
  private static final String VALID_PREFIX = "prefix";
  private static final String VALID_NAME = "object key";

  @MockBean
  private S3Client s3Client;

  @Autowired
  private ObjectStoreClient s3ClientWrapper;

  @Configuration
  @EnableRetry
  public static class Config {

    @Bean
    public ObjectStoreClient createS3ClientWrapper(S3Client s3Client) {
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

    List<S3Object> actResult = s3ClientWrapper.getObjects(VALID_BUCKET_NAME, VALID_PREFIX);

    assertThat(actResult).isEqualTo(expResult);
  }

  private static Stream<Arguments> createGetObjectsResults() {
    return Stream.of(
        Lists.emptyList(),
        Lists.list(new S3Object("objName", "eTag")),
        Lists.list(new S3Object("objName1", "eTag1"), new S3Object("objName2", "eTag2"))
    ).map(Arguments::of);
  }

  private ListObjectsV2Response buildListObjectsResponse(List<S3Object> s3Objects) {
    var responseObjects = s3Objects.stream().map(
        s3Object -> software.amazon.awssdk.services.s3.model.S3Object.builder()
            .key(s3Object.getObjectName())
            .eTag(s3Object.getEtag()))
        .map(SdkBuilder::build).collect(Collectors.toList());
    return ListObjectsV2Response.builder().contents(responseObjects).build();
  }

  @Test
  void getObjectsRemovesDoubleQuotesFromEtags() {
    String expEtag = "eTag";
    ListObjectsV2Response actResponse =
        buildListObjectsResponse(List.of(new S3Object(VALID_NAME, "\"" + expEtag + "\"")));
    when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(actResponse);

    List<S3Object> actResult = s3ClientWrapper.getObjects(VALID_BUCKET_NAME, VALID_PREFIX);

    assertThat(actResult).isEqualTo(List.of(new S3Object(VALID_NAME, expEtag)));
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
  void shouldAttemptToGetObjectsThreeTimesAndThenThrow(Class<Exception> cause) {
    when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenThrow(cause);
    assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> s3ClientWrapper.getObjects(VALID_BUCKET_NAME, VALID_PREFIX));

    verify(s3Client, times(3)).listObjectsV2(any(ListObjectsV2Request.class));
  }

  @Test
  void testPutObjectForNoHeaders() {
    s3ClientWrapper.putObject(VALID_BUCKET_NAME, VALID_NAME, Path.of(""), emptyMap());

    PutObjectRequest expRequest = PutObjectRequest.builder().bucket(VALID_BUCKET_NAME).key(VALID_NAME).build();
    verify(s3Client, atLeastOnce()).putObject(eq(expRequest), any(RequestBody.class));
  }

  @Test
  void testPutObjectForCacheControlHeader() {
    var expCacheControl = "foo-cache-control";
    s3ClientWrapper
        .putObject(VALID_BUCKET_NAME, VALID_NAME, Path.of(""), newHashMap(HeaderKey.CACHE_CONTROL, expCacheControl));

    PutObjectRequest expRequest =
        PutObjectRequest.builder().bucket(VALID_BUCKET_NAME).key(VALID_NAME).cacheControl(expCacheControl).build();
    verify(s3Client, atLeastOnce()).putObject(eq(expRequest), any(RequestBody.class));
  }

  @Test
  void testPutObjectForAmzAclHeader() {
    String expAcl = "foo-acl";
    s3ClientWrapper.putObject(VALID_BUCKET_NAME, VALID_NAME, Path.of(""), newHashMap(HeaderKey.AMZ_ACL, expAcl));

    PutObjectRequest expRequest =
        PutObjectRequest.builder().bucket(VALID_BUCKET_NAME).key(VALID_NAME).acl(expAcl).build();
    verify(s3Client, atLeastOnce()).putObject(eq(expRequest), any(RequestBody.class));
  }

  @ParameterizedTest
  @ValueSource(classes = {NoSuchBucketException.class, S3Exception.class, SdkClientException.class, SdkException.class})
  void putObjectsThrowsObjectStoreOperationFailedExceptionIfClientThrows(Class<Exception> cause) {
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenThrow(cause);
    assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> s3ClientWrapper.putObject(VALID_BUCKET_NAME, VALID_PREFIX, Path.of(""), emptyMap()));
  }

  @ParameterizedTest
  @ValueSource(classes = {NoSuchBucketException.class, S3Exception.class, SdkClientException.class, SdkException.class})
  void shouldAttemptToUploadObjectThreeTimesAndThenThrow(Class<Exception> cause) {
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenThrow(cause);
    assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> s3ClientWrapper.putObject(VALID_BUCKET_NAME, VALID_PREFIX, Path.of(""), emptyMap()));

    verify(s3Client, times(3)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
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

    assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> s3ClientWrapper.removeObjects(VALID_BUCKET_NAME, List.of(VALID_NAME)));
  }

  @ParameterizedTest
  @ValueSource(classes = {NoSuchBucketException.class, S3Exception.class, SdkClientException.class, SdkException.class})
  void removeObjectsThrowsObjectStoreOperationFailedExceptionIfClientThrows(Class<Exception> cause) {
    when(s3Client.deleteObjects(any(DeleteObjectsRequest.class))).thenThrow(cause);
    assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> s3ClientWrapper.removeObjects(VALID_BUCKET_NAME, List.of(VALID_NAME)));
  }

  @ParameterizedTest
  @ValueSource(classes = {NoSuchBucketException.class, S3Exception.class, SdkClientException.class, SdkException.class})
  void shouldAttemptToRemoveObjectThreeTimesAndThenThrow(Class<Exception> cause) {
    when(s3Client.deleteObjects(any(DeleteObjectsRequest.class))).thenThrow(cause);
    assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> s3ClientWrapper.removeObjects(VALID_BUCKET_NAME, List.of(VALID_NAME)));

    verify(s3Client, times(3)).deleteObjects(any(DeleteObjectsRequest.class));
  }
}
