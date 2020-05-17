package app.coronawarn.server.services.distribution.objectstore;

import app.coronawarn.server.services.distribution.objectstore.publish.MetadataProvider;
import app.coronawarn.server.services.distribution.objectstore.publish.LocalFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * <p>Grants access to the object store, enabling basic functionality for working with files.</p>
 * <p>Use S3Publisher for more convenient access.</p>
 * <br>
 * Make sure the following ENV vars are available.
 * <ul>
 * <li>cwa.objectstore.endpoint</li>
 * <li>cwa.objectstore.bucket</li>
 * <li>AWS_ACCESS_KEY_ID</li>
 * <li>AWS_SECRET_ACCESS_KEY</li>
 * </ul>
 */
@Component
public class ObjectStoreAccess implements MetadataProvider {

  private static final Logger logger = LoggerFactory.getLogger(ObjectStoreAccess.class);

  private final String bucket;

  private S3Client client;

  @Autowired
  public ObjectStoreAccess(@Value("${cwa.objectstore.endpoint:notset}") String endpoint,
      @Value("${cwa.objectstore.bucket:notset}") String bucket) throws URISyntaxException {
    this.bucket = bucket;

    if ("notset".equals(endpoint) || "notset".equals(bucket)) {
      logger.error("S3 Connection parameters missing - unable to serve S3 integration.");
      return;
    }

    this.client = S3Client.builder()
        .endpointOverride(new URI(endpoint))
        .region(Region.EU_CENTRAL_1) /* required by SDK, but ignored on S3 side */
        .build();
  }

  /**
   * Stores the target file on the S3.
   *
   * @param localFile the file to be published
   */
  public void putObject(LocalFile localFile) {
    logger.info("... uploading " + localFile.getS3Key());
    RequestBody bodyFile = RequestBody.fromFile(localFile.getFile());

    this.client.putObject(PutObjectRequest.builder()
        .bucket(this.bucket)
        .key(localFile.getS3Key())
        .metadata(createMetadataFor(localFile))
        .build(),
        bodyFile);
  }

  /**
   * Deletes objects in the object store, based on the given prefix (folder structure).
   *
   * @param prefix the prefix, e.g. my/folder/
   */
  public void deleteObjectsWithPrefix(String prefix) {
    Stream<S3Object> files = getObjectsWithPrefix(prefix);
    logger.info("Deleting num files: " + files);
    Collection<ObjectIdentifier> identifiers = files
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
  public Stream<S3Object> getObjectsWithPrefix(String prefix) {
    ListObjectsV2Request listReq = ListObjectsV2Request.builder()
        .bucket(bucket)
        .prefix(prefix)
        .build();

    return this.client
        .listObjectsV2Paginator(listReq)
        .stream()
        .flatMap(page -> page.contents().stream());
  }

  @Override
  public Map<String, String> fetchMetadataFor(String s3Key) {
    return this.client.headObject(HeadObjectRequest.builder()
        .bucket(bucket)
        .key(s3Key)
        .build())
        .metadata();
  }

  private Map<String, String> createMetadataFor(LocalFile file) {
    return Map.of("cwa.hash", file.getHash());
  }
}
