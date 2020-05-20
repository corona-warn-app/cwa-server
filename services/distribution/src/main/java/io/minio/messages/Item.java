/*
 * MinIO Java SDK for Amazon S3 Compatible Cloud Storage, (C) 2015 MinIO, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.minio.messages;

import java.time.ZonedDateTime;
import java.util.Map;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/* ----------------------------------------------------------------
 * Copied from MinIO due to patch not yet available.
 * https://github.com/minio/minio-java/pull/921
 * Waiting for new release version: 7.0.3
 * ----------------------------------------------------------------
 */

/**
 * Helper class to denote Object information in {@link ListBucketResult} and {@link
 * ListBucketResultV1}.
 */
@Root(name = "Contents", strict = false)
public class Item {

  @Element(name = "Key")
  private String objectName;

  @Element(name = "LastModified")
  private ResponseDate lastModified;

  @Element(name = "ETag")
  private String etag;

  @Element(name = "Size")
  private long size;

  @Element(name = "StorageClass")
  private String storageClass;

  @Element(name = "Owner", required = false) /* Monkeypatch: Owner should be optional */
  private Owner owner;

  @Element(name = "UserMetadata", required = false)
  private Metadata userMetadata;

  private boolean isDir = false;

  public Item() {

  }

  /**
   * Constructs a new Item for prefix i.e. directory.
   */
  public Item(String prefix) {
    this.objectName = prefix;
    this.isDir = true;
  }

  /**
   * Returns object name.
   */
  public String objectName() {
    return objectName;
  }

  /**
   * Returns last modified time of the object.
   */
  public ZonedDateTime lastModified() {
    return lastModified.zonedDateTime();
  }

  /**
   * Returns ETag of the object.
   */
  public String etag() {
    return etag;
  }

  /**
   * Returns object size.
   */
  public long size() {
    return size;
  }

  /**
   * Returns storage class of the object.
   */
  public String storageClass() {
    return storageClass;
  }

  /**
   * Returns owner object of given the object.
   */
  public Owner owner() {
    return owner;
  }

  /**
   * Returns user metadata. This is MinIO specific extension to ListObjectsV2.
   */
  public Map<String, String> userMetadata() {
    if (userMetadata == null) {
      return null;
    }

    return userMetadata.get();
  }

  /**
   * Returns whether the object is a directory or not.
   */
  public boolean isDir() {
    return isDir;
  }
}
