package app.coronawarn.server.services.distribution.objectstore;

import io.minio.messages.Item;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an object as discovered on S3.
 */
public class S3Object {

  /**
   * the name of the object.
   */
  private final String objectName;

  /**
   * the available meta information.
   */
  private Map<String, String> metadata = new HashMap<>();

  /**
   * Constructs a new S3Object for the given object name.
   *
   * @param objectName the target object name
   */
  public S3Object(String objectName) {
    this.objectName = objectName;
  }

  public String getObjectName() {
    return objectName;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  /**
   * Returns a new instance of an S3Object based on the given item.
   *
   * @param item the item (as provided by MinIO)
   * @return the S3Object representation
   */
  public static S3Object of(Item item) {
    S3Object s3Object = new S3Object(item.objectName());

    if (item.userMetadata() != null) {
      s3Object.metadata = item.userMetadata();
    }

    return s3Object;
  }
}
