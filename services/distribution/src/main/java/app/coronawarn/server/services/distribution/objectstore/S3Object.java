package app.coronawarn.server.services.distribution.objectstore;

import io.minio.messages.Item;
import java.util.HashMap;
import java.util.Map;

public class S3Object {

  private String objectName;

  private Map<String, String> metadata = new HashMap<>();

  public S3Object(String objectName) {
    this.objectName = objectName;
  }

  public String getObjectName() {
    return objectName;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public static S3Object of(Item item) {
    S3Object s3Object = new S3Object(item.objectName());

    if (item.userMetadata() != null) {
      s3Object.metadata = item.userMetadata();
    }

    return s3Object;
  }
}
