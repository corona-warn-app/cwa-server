/*
 * Corona-Warn-App
 *
 * Deutsche Telekom AG, SAP SE and all other contributors /
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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * <p>Grants access to the S3 compatible object storage hosted by Telekom in Germany, enabling basic
 * functionality for working with files.</p>
 * <p>Use S3Publisher for more convenient access.</p>
 * <br>
 * Make sure the following ENV vars are available.
 * <ul>
 *   <li>cwa.objectstore.endpoint</li>
 *   <li>cwa.objectstore.bucket</li>
 *   <li>AWS_ACCESS_KEY_ID</li>
 *   <li>AWS_SECRET_ACCESS_KEY</li>
 * </ul>
 */
@Component
public class ObjectStoreAccess {

  private static final Logger logger = LoggerFactory.getLogger(ObjectStoreAccess.class);

  private S3Client client;

  @Value("${cwa.objectstore.bucket}")
  private String bucket;
  
  /**
   * Constructs an {@link ObjectStoreAccess} instance for communication with the specified object store endpoint and
   * bucket.
   *
   * @param endpoint The endpoint URI for communication with the object store.
   * @throws URISyntaxException thrown if endpoint URI invalid.
   */
  @Autowired
  public ObjectStoreAccess(@Value("${cwa.objectstore.endpoint}") String endpoint) throws URISyntaxException {
    this.client = S3Client.builder()
        .endpointOverride(new URI(endpoint))
        .region(Region.EU_CENTRAL_1) /* required by SDK, but ignored on S3 side */
        .build();
  }

  /**
   * Stores an object in the object store.
   *
   * @param key  the key to use, e.g. my/folder/struc/file.ext
   * @param file the file to upload
   */
  public void putObject(String key, File file) {
    RequestBody bodyFile = RequestBody.fromFile(file);

    this.client
        .putObject(PutObjectRequest.builder().bucket(this.bucket).key(key).build(), bodyFile);
  }

  /**
   * Deletes objects in the object store, based on the given prefix (folder structure).
   *
   * @param prefix the prefix, e.g. my/folder/
   */
  public void deleteObjectsWithPrefix(String prefix) {
    ListObjectsV2Response files = getObjectsWithPrefix(prefix);
    Collection<ObjectIdentifier> identifiers = files
        .contents()
        .stream()
        .map(s3object -> ObjectIdentifier.builder().key(s3object.key()).build())
        .collect(Collectors.toList());

    this.client.deleteObjects(DeleteObjectsRequest.builder().bucket(this.bucket).delete(
        Delete.builder().objects(identifiers).build()).build());
  }

  /**
   * Fetches the list of objects in the store with the given prefix.
   *
   * @param prefix the prefix, e.g. my/folder/
   * @return the list of objects
   */
  public ListObjectsV2Response getObjectsWithPrefix(String prefix) {
    return client
        .listObjectsV2(ListObjectsV2Request.builder().prefix(prefix).bucket(this.bucket).build());
  }

}
