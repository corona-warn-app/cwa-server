package app.coronawarn.server.services.distribution.objectstore.publish;

import app.coronawarn.server.services.distribution.objectstore.S3Object;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides an overview about which files are currently available on S3.
 */
public class PublishedFileSet {

  /** ta map of S3 objects with the S3 object name as the key component of the map. */
  private Map<String, S3Object> s3Objects;

  /**
   * Creates a new PublishedFileSet for the given S3 objects with the help of the metadata provider.
   * The metadata provider helps to determine whether files have been changed, and are requiring
   * re-upload.
   *
   * @param s3Objects the list of s3 objects.
   */
  public PublishedFileSet(List<S3Object> s3Objects) {
    this.s3Objects = s3Objects.stream()
        .collect(Collectors.toMap(S3Object::getObjectName, s3object -> s3object));
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
  public boolean isNotYetPublished(LocalFile file) {
    S3Object published = s3Objects.get(file.getS3Key());

    if (published == null) {
      return true;
    }

    return !hasSameHashAsPublishedFile(file);
  }

  private boolean hasSameHashAsPublishedFile(LocalFile file) {
    var metaData = s3Objects.get(file.getS3Key()).getMetadata();

    String s3FileHash = metaData.get("X-Amz-Meta-Cwa.hash");

    return file.getHash().equals(s3FileHash);
  }

}
