package app.coronawarn.server.services.distribution.objectstore.publish;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * Provides an overview about which files are currently available on S3.
 */
public class PublishedFileSet {

  private final MetadataProvider metadataProvider;

  private Map<String, S3Object> s3Objects;

  public PublishedFileSet(Stream<S3Object> s3Objects, MetadataProvider metadataProvider) {
    this.s3Objects = s3Objects
        .collect(Collectors.toMap(s3object -> s3object.key(), s3object -> s3object));
    this.metadataProvider = metadataProvider;
  }

  /**
   * Checks whether the given file, which is subject for publishing, is already available on the S3.
   * Will return true, when:
   * <ul>
   *   <li>The S3 object key exists on S3</li>
   *   <li>The checksum of the existing S3 object matches the hash of the given file</li>
   * </ul>
   *
   * @param file the to-be-published file which should be checked
   * @return true, if it exists & is identical
   */
  public boolean isNotYetPublished(PublishFile file) {
    S3Object published = s3Objects.get(file.s3Key);

    if (published == null) {
      return true;
    }

    return !hasSameHashAsPublishedFile(file);
  }

  private boolean hasSameHashAsPublishedFile(PublishFile file) {
    String hash = metadataProvider.fetchMetadataFor(file.s3Key).get("cwa.hash");

    return file.getHash().equals(hash);
  }
}
