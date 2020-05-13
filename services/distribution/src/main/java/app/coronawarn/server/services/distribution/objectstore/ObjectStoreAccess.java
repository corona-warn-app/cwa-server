package app.coronawarn.server.services.distribution.objectstore;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
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

@Component
public class ObjectStoreAccess {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  private String bucket;

  private S3Client client;

  @Autowired
  public ObjectStoreAccess(@Value("${cwa.objectstore.endpoint}") String endpoint,
      @Value("${cwa.objectstore.bucket}") String bucket) throws URISyntaxException {
    this.bucket = bucket;
    this.client = S3Client.builder()
        .endpointOverride(new URI(endpoint))
        .region(Region.EU_CENTRAL_1)
        .build();
  }

  public void put(String key, File file) {
    RequestBody bodyFile = RequestBody.fromFile(file);

    this.client
        .putObject(PutObjectRequest.builder().bucket(this.bucket).key(key).build(), bodyFile);
  }

  public void deleteFilesWithPrefix(String prefix) {
    var files = getFilesWithPrefix(prefix);
    var identifiers = files
        .contents()
        .stream()
        .map(s3object -> ObjectIdentifier.builder().key(s3object.key()).build())
        .collect(Collectors.toList());

    this.client.deleteObjects(DeleteObjectsRequest.builder().bucket(this.bucket).delete(
        Delete.builder().objects(identifiers).build()).build());
  }

  public ListObjectsV2Response getFilesWithPrefix(String prefix) {
    return client
        .listObjectsV2(ListObjectsV2Request.builder().prefix(prefix).bucket(this.bucket).build());
  }

  public void printAllFiles() {
    var out = client.listObjectsV2(ListObjectsV2Request.builder().bucket(this.bucket).build());

    logger.info("-------");
    logger.info(out.contents().toString());
    logger.info("-------");

    logger.info("Fetched S3");
  }

}
