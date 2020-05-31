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
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.util.IterableUtil.iterable;
import static org.assertj.core.util.Lists.list;
import static org.assertj.core.util.Maps.newHashMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreClient.HeaderKey;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.XmlParserException;
import io.minio.messages.DeleteError;
import io.minio.messages.Item;
import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

class MinioClientWrapperTest {

  private static final String VALID_BUCKET_NAME = "myBucket";
  private static final String VALID_PREFIX = "prefix";
  private static final String VALID_NAME = "object key";

  private MinioClient minioClient;
  private MinioClientWrapper minioClientWrapper;

  @BeforeEach
  public void setUpMocks() {
    minioClient = mock(MinioClient.class);
    minioClientWrapper = new MinioClientWrapper(minioClient);
  }

  @Test
  void testBucketExistsIfBucketExists() throws Exception {
    when(minioClient.bucketExists(any())).thenReturn(true);
    assertThat(minioClientWrapper.bucketExists(VALID_BUCKET_NAME)).isTrue();
  }

  @Test
  void testBucketExistsIfBucketDoesNotExist() throws Exception {
    when(minioClient.bucketExists(any())).thenReturn(false);
    assertThat(minioClientWrapper.bucketExists(VALID_BUCKET_NAME)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(classes = {ErrorResponseException.class, InsufficientDataException.class,
      InternalException.class, IllegalArgumentException.class, InvalidBucketNameException.class,
      InvalidKeyException.class, InvalidResponseException.class,
      IOException.class, NoSuchAlgorithmException.class, XmlParserException.class})
  void bucketExistsThrowsObjectStoreOperationFailedExceptionIfClientThrows(Class<Exception> cause) throws Exception {
    when(minioClient.bucketExists(any())).thenThrow(cause);
    assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> minioClientWrapper.bucketExists(VALID_BUCKET_NAME));
  }

  @Test
  void testGetObjectsSendsCorrectRequest() {
    when(minioClient.listObjects(anyString(), anyString(), anyBoolean())).thenReturn(iterable());
    minioClientWrapper.getObjects(VALID_BUCKET_NAME, VALID_PREFIX);
    verify(minioClient, atLeastOnce()).listObjects(eq(VALID_BUCKET_NAME), eq(VALID_PREFIX), eq(true));
  }

  @ParameterizedTest
  @MethodSource("createGetObjectsResults")
  void testGetObjects(List<S3Object> expResult) throws Exception {
    Iterable<Result<Item>> actResponse = buildListObjectsResponse(expResult);
    when(minioClient.listObjects(anyString(), anyString(), anyBoolean())).thenReturn(actResponse);

    List<S3Object> actResult = minioClientWrapper.getObjects(VALID_BUCKET_NAME, VALID_PREFIX);

    assertThat(actResult).isEqualTo(expResult);
  }

  private static Stream<Arguments> createGetObjectsResults() {
    return Stream.of(
        Lists.emptyList(),
        list(new S3Object("objName", "eTag")),
        list(new S3Object("objName1", "eTag1"), new S3Object("objName2", "eTag2"))
    ).map(Arguments::of);
  }

  private Iterable<Result<Item>> buildListObjectsResponse(List<S3Object> s3Objects) throws Exception {
    List<Result<Item>> response = new ArrayList<>(s3Objects.size());

    for (S3Object s3Object : s3Objects) {
      Item item = mock(Item.class);
      Result<Item> result = mock(Result.class);
      when(result.get()).thenReturn(item);
      when(item.etag()).thenReturn(s3Object.getEtag());
      when(item.objectName()).thenReturn(s3Object.getObjectName());
      response.add(result);
    }

    return iterable(response.toArray(new Result[response.size()]));
  }

  @Test
  void getObjectsRemovesDoubleQuotesFromEtags() throws Exception {
    String expEtag = "eTag";
    Iterable<Result<Item>> actResponse = buildListObjectsResponse(
        List.of(new S3Object(VALID_NAME, "\"" + expEtag + "\"")));
    when(minioClient.listObjects(anyString(), anyString(), anyBoolean())).thenReturn(actResponse);

    List<S3Object> actResult = minioClientWrapper.getObjects(VALID_BUCKET_NAME, VALID_PREFIX);

    assertThat(actResult).isEqualTo(List.of(new S3Object(VALID_NAME, expEtag)));
  }

  @ParameterizedTest
  @ValueSource(classes = {ErrorResponseException.class, InsufficientDataException.class, XmlParserException.class,
      InternalException.class, InvalidBucketNameException.class, InvalidKeyException.class, JsonParseException.class,
      InvalidResponseException.class, JsonMappingException.class, IOException.class, NoSuchAlgorithmException.class,
      IllegalArgumentException.class})
  void getObjectsThrowsObjectStoreOperationFailedExceptionIfClientThrows(Class<Exception> cause) throws Exception {
    Result<Item> actResult = mock(Result.class);
    when(actResult.get()).thenThrow(cause);
    when(minioClient.listObjects(anyString(), anyString(), anyBoolean())).thenReturn(iterable(actResult));
    assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> minioClientWrapper.getObjects(VALID_BUCKET_NAME, VALID_PREFIX));
  }

  @Test
  void testPutObjectForNoHeaders() throws Exception {
    minioClientWrapper.putObject(VALID_BUCKET_NAME, VALID_NAME, Path.of(""), emptyMap());
    ArgumentCaptor<PutObjectOptions> options = ArgumentCaptor.forClass(PutObjectOptions.class);
    verify(minioClient, atLeastOnce()).putObject(eq(VALID_BUCKET_NAME), eq(VALID_NAME), eq(""), options.capture());
    assertThat(options.getValue().headers()).isEmpty();
  }

  @Test
  void testPutObjectForCacheControlHeader() throws Exception {
    String expCacheControl = "foo-cache-control";

    minioClientWrapper.putObject(VALID_BUCKET_NAME, VALID_NAME, Path.of(""),
        newHashMap(HeaderKey.CACHE_CONTROL, expCacheControl));

    ArgumentCaptor<PutObjectOptions> options = ArgumentCaptor.forClass(PutObjectOptions.class);
    verify(minioClient, atLeastOnce()).putObject(eq(VALID_BUCKET_NAME), eq(VALID_NAME), eq(""), options.capture());
    assertThat(options.getValue().headers()).hasSize(1);
    assertThat(options.getValue().headers()).contains(entry(HeaderKey.CACHE_CONTROL.keyValue, expCacheControl));
  }

  @Test
  void testPutObjectForAmzAclHeader() throws Exception {
    String expAcl = "foo-acl";

    minioClientWrapper.putObject(VALID_BUCKET_NAME, VALID_NAME, Path.of(""), newHashMap(HeaderKey.AMZ_ACL, expAcl));

    ArgumentCaptor<PutObjectOptions> options = ArgumentCaptor.forClass(PutObjectOptions.class);
    verify(minioClient, atLeastOnce()).putObject(eq(VALID_BUCKET_NAME), eq(VALID_NAME), eq(""), options.capture());
    assertThat(options.getValue().headers()).hasSize(1);
    assertThat(options.getValue().headers()).contains(entry(HeaderKey.AMZ_ACL.keyValue, expAcl));
  }

  @ParameterizedTest
  @ValueSource(classes = {ErrorResponseException.class, IllegalArgumentException.class, InsufficientDataException.class,
      InternalException.class, InvalidBucketNameException.class, InvalidKeyException.class, XmlParserException.class,
      IOException.class, NoSuchAlgorithmException.class, InvalidResponseException.class})
  void putObjectsThrowsObjectStoreOperationFailedExceptionIfClientThrows(Class<Exception> cause) throws Exception {
    doThrow(cause).when(minioClient).putObject(anyString(), anyString(), anyString(), any(PutObjectOptions.class));
    assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> minioClientWrapper.putObject(VALID_BUCKET_NAME, VALID_PREFIX, Path.of(""), emptyMap()));
  }

  @Test
  void testRemoveObjects() {
    when(minioClient.removeObjects(anyString(), anyList())).thenReturn(iterable());
    List<String> expObjectNames = List.of("obj1", "obj2");

    minioClientWrapper.removeObjects(VALID_BUCKET_NAME, expObjectNames);

    verify(minioClient, atLeastOnce()).removeObjects(eq(VALID_BUCKET_NAME), eq(expObjectNames));
  }

  @Test
  void removeObjectsThrowsObjectStoreOperationFailedExceptionOnDeleteErrors() {
    Result<DeleteError> result = mock(Result.class);
    when(minioClient.removeObjects(anyString(), anyList())).thenReturn(iterable(result));
    assertThatExceptionOfType(ObjectStoreOperationFailedException.class)
        .isThrownBy(() -> minioClientWrapper.removeObjects(VALID_BUCKET_NAME, list(VALID_NAME)));
  }
}
