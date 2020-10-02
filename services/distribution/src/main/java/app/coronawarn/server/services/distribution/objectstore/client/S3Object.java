

package app.coronawarn.server.services.distribution.objectstore.client;

import java.util.Objects;

/**
 * Represents an object as discovered on S3.
 */
public class S3Object {

  /**
   * the name of the object.
   */
  private final String objectName;

  /** The cwaHash of this S3 Object. */
  private String cwaHash;

  /**
   * Constructs a new S3Object for the given object name.
   *
   * @param objectName the target object name
   */
  public S3Object(String objectName) {
    this.objectName = objectName;
  }

  /**
   * Constructs a new S3Object for the given object name.
   *
   * @param objectName the target object name
   * @param cwaHash the checksum for that file
   */
  public S3Object(String objectName, String cwaHash) {
    this(objectName);
    this.cwaHash = cwaHash;
  }

  public String getObjectName() {
    return objectName;
  }

  public String getCwaHash() {
    return cwaHash;
  }

  /**
   * Indicates if the S3 object is a file with diagnosis key content.
   * The evaluation is based on the distribution logic which implies that such files are generated
   * with a Date / Hour S3 key format (days: 1-31 / hours: 0-23) ending in 2 digits.
   */
  public boolean isDiagnosisKeyFile() {
    return Objects.nonNull(objectName) && objectName.matches(".*\\d\\d");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    S3Object s3Object = (S3Object) o;
    return Objects.equals(objectName, s3Object.objectName) && Objects.equals(cwaHash, s3Object.cwaHash);
  }

  @Override
  public int hashCode() {
    return Objects.hash(objectName, cwaHash);
  }
}
